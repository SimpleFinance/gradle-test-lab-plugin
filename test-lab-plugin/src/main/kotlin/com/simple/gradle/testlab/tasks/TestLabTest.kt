package com.simple.gradle.testlab.tasks

import com.google.api.services.testing.model.AndroidDevice
import com.google.api.services.testing.model.AndroidDeviceList
import com.google.api.services.testing.model.ClientInfo
import com.google.api.services.testing.model.ClientInfoDetail
import com.google.api.services.testing.model.EnvironmentMatrix
import com.google.api.services.testing.model.GoogleCloudStorage
import com.google.api.services.testing.model.ResultStorage
import com.google.api.services.testing.model.TestMatrix
import com.google.api.services.testing.model.ToolResultsHistory
import com.simple.gradle.testlab.internal.GoogleApiInternal
import com.simple.gradle.testlab.internal.MatrixMonitor
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.ToolResultsHistoryPicker
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.artifacts.ArtifactFetcherFactory
import com.simple.gradle.testlab.internal.createToolResultsUiUrl
import com.simple.gradle.testlab.internal.getToolResultsIds
import com.simple.gradle.testlab.internal.log
import com.simple.gradle.testlab.model.GoogleApi
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File

open class TestLabTest : DefaultTask() {
    @Internal private val objects = project.objects

    @get:InputFile val appApk: Property<File> = objects.property()
    @get:InputFile @get:Optional val testApk: Property<File> = objects.property()
    @get:Input @get:Optional val appPackageId: Property<String?> = objects.property()
    @get:Input @get:Optional val testPackageId: Property<String?> = objects.property()
    @get:Input val google: Property<GoogleApi> = objects.property()

    @get:Input internal val testConfig: Property<TestConfigInternal> = objects.property()

    @get:OutputDirectory val outputDir: Property<File> = objects.property()

    @get:Internal internal lateinit var prefix: String
    @get:Internal internal lateinit var uploadResults: UploadResults

    init {
        group = "verification"
        description = "Run tests on Firebase Test Lab."
    }

    private val googleConfig by lazy { google.get() }
    private val googleApi by lazy { GoogleApiInternal(googleConfig) }
    private val bucketName by lazy { googleConfig.bucketName ?: googleApi.defaultBucketName() }
    private val gcsBucketPath by lazy { "gs://$bucketName/$prefix" }

    @TaskAction
    fun runTest() {
        val testConfig = testConfig.get()
        val appApkReference = uploadResults.references[appApk.get()]
                ?: throw GradleException("App APK not found: ${appApk.get()}")
        val testApkReference = if (testConfig.requiresTestApk) {
            uploadResults.references[testApk.get()]
                ?: throw GradleException("Test APK not found: ${testApk.get()}")
        } else {
            null
        }

        val historyPicker = ToolResultsHistoryPicker(googleConfig.projectId!!, googleApi)
        val historyName = historyPicker.pickHistoryName(testConfig.resultsHistoryName, appPackageId.orNull)
        val historyId = historyPicker.getToolResultsHistoryId(historyName)

        val testMatrix = TestMatrix()
                .setClientInfo(clientInfo())
                .setResultStorage(resultStorage(historyId))
                .setEnvironmentMatrix(EnvironmentMatrix().setAndroidDeviceList(androidDeviceList()))
                .setTestSpecification(testConfig.testSpecification(appApkReference, testApkReference))

        log.info("Test matrix: ${testMatrix.toPrettyString()}")

        val triggeredTestMatrix = googleApi.testing.projects().testMatrices()
                .create(googleConfig.projectId, testMatrix)
                .execute()

        log.info("Triggered matrix: ${triggeredTestMatrix.toPrettyString()}")

        val projectId = triggeredTestMatrix.projectId
        val matrixId = triggeredTestMatrix.testMatrixId

        val monitor = MatrixMonitor(googleApi, projectId, matrixId, testConfig.testType)

        val canceler = Thread { monitor.cancelTestMatrix() }
        Runtime.getRuntime().addShutdownHook(canceler)

        val supportedExecutions = monitor.handleUnsupportedExecutions(triggeredTestMatrix)
        val toolResultsIds = getToolResultsIds(triggeredTestMatrix, monitor)
        val url = createToolResultsUiUrl(projectId, toolResultsIds)
        log.lifecycle("Test results will be streamed to [$url].")

        if (supportedExecutions.size == 1) {
            monitor.monitorTestExecutionProgress(supportedExecutions[0].id)
        } else {
            monitor.monitorTestMatrixProgress()
        }

        Runtime.getRuntime().removeShutdownHook(canceler)

        log.lifecycle("More results are available at [$url].")

        if (testConfig.artifacts.isNotEmpty()) {
            with (ArtifactFetcherFactory(googleApi.storage.objects(), bucketName, prefix, outputDir.get())) {
                for (test in supportedExecutions) {
                    val suffix = with (test.environment.androidDevice) {
                        "$androidModelId-$androidVersionId-$locale-$orientation"
                    }
                    log.lifecycle("Fetching result artifacts for $suffix...")
                    testConfig.artifacts
                        .forEach { createFetcher(suffix, it).fetch() }
                }
            }
        }
    }

    private fun resultStorage(historyId: String?): ResultStorage = ResultStorage()
            .setGoogleCloudStorage(GoogleCloudStorage().setGcsPath(gcsBucketPath))
            .apply {
                if (historyId != null) {
                    toolResultsHistory = ToolResultsHistory()
                            .setProjectId(googleConfig.projectId!!)
                            .setHistoryId(historyId)
                }
            }

    // Mimic the `gcloud` tool; useOrchestrator does not work without this.
    private fun clientInfo(): ClientInfo = ClientInfo()
            .setName("gcloud")
            .setClientInfoDetails(listOf(
                    ClientInfoDetail().setKey("Cloud SDK Version").setValue("178.0.0"),
                    ClientInfoDetail().setKey("Release Track").setValue("GA")
            ))

    private fun androidDeviceList(): AndroidDeviceList = AndroidDeviceList()
            .setAndroidDevices(testConfig.get().devices.map {
                AndroidDevice()
                        .setAndroidModelId(it.modelId)
                        .setAndroidVersionId(it.version.toString())
                        .setLocale(it.locale)
                        .setOrientation(it.orientation.name.toLowerCase())
            })
}

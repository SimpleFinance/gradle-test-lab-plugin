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
import com.simple.gradle.testlab.internal.ArtifactFetcher
import com.simple.gradle.testlab.internal.GoogleApiInternal
import com.simple.gradle.testlab.internal.MatrixMonitor
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.ToolResultsHistoryPicker
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.createToolResultsUiUrl
import com.simple.gradle.testlab.internal.getToolResultsIds
import com.simple.gradle.testlab.model.GoogleApi
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.reflect.KProperty

open class TestLabTest : DefaultTask() {
    private val objects = project.objects

    @delegate:InputFile val appApk: Property<File> by objects
    @delegate:InputFile @delegate:Optional val testApk: Property<File> by objects
    @delegate:Input @delegate:Optional val appPackageId: Property<String?> by objects
    @delegate:Input @delegate:Optional val testPackageId: Property<String?> by objects
    @delegate:Input val google: Property<GoogleApi> by objects
    @delegate:Input internal val testConfig: Property<TestConfigInternal> by objects

    @delegate:OutputDirectory val outputDir: Property<File> by project.objects

    @Internal internal lateinit var prefix: String
    @Internal internal lateinit var uploadResults: UploadResults

    init {
        group = "verification"
        description = "Run tests on Firebase Test Lab."
    }

    @delegate:Internal private val googleConfig by lazy { google.get() }
    @delegate:Internal private val googleApi by lazy { GoogleApiInternal(googleConfig) }
    @delegate:Internal private val bucketName by lazy { googleConfig.bucketName ?: googleApi.defaultBucketName() }
    @delegate:Internal private val gcsBucketPath by lazy { "gs://$bucketName/$prefix" }

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

        logger.info("Test matrix: ${testMatrix.toPrettyString()}")

        val triggeredTestMatrix = googleApi.testing.projects().testMatrices()
                .create(googleConfig.projectId, testMatrix)
                .execute()

        logger.info("Triggered matrix: ${triggeredTestMatrix.toPrettyString()}")

        val projectId = triggeredTestMatrix.projectId
        val matrixId = triggeredTestMatrix.testMatrixId

        val monitor = MatrixMonitor(googleApi, projectId, matrixId, testConfig.testType, logger)

        val canceler = Thread { monitor.cancelTestMatrix() }
        Runtime.getRuntime().addShutdownHook(canceler)

        val supportedExecutions = monitor.handleUnsupportedExecutions(triggeredTestMatrix)
        val toolResultsIds = getToolResultsIds(triggeredTestMatrix, monitor)
        val url = createToolResultsUiUrl(projectId, toolResultsIds)
        logger.lifecycle("Test results will be streamed to [$url].")

        if (supportedExecutions.size == 1) {
            monitor.monitorTestExecutionProgress(supportedExecutions[0].id)
        } else {
            monitor.monitorTestMatrixProgress()
        }

        Runtime.getRuntime().removeShutdownHook(canceler)

        logger.lifecycle("More results are available at [$url].")

        if (testConfig.hasArtifacts) {
            val fetcher = ArtifactFetcher(project, googleApi, bucketName, prefix, outputDir.get(), logger)
            for (test in supportedExecutions) {
                fetcher.fetch(test.environment.androidDevice, testConfig.artifacts)
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

    private inline operator fun <reified T> ObjectFactory.getValue(thisRef: Any?, property: KProperty<*>): Property<T> =
        property(T::class.java)
}

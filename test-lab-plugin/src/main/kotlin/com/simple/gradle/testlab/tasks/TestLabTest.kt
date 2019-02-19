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
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.MatrixMonitor
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.ToolResultsHistoryPicker
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.artifacts.ArtifactFetcherFactory
import com.simple.gradle.testlab.internal.asDeviceFileReference
import com.simple.gradle.testlab.internal.asFileReference
import com.simple.gradle.testlab.internal.createToolResultsUiUrl
import com.simple.gradle.testlab.internal.getToolResultsIds
import com.simple.gradle.testlab.internal.log
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class TestLabTest @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    @Input @Optional val appPackageId: Property<String?> = objects.property()
    @Input val googleApiConfig: Property<GoogleApiConfig> = objects.property()
    @Input internal val prefix: Property<String> = objects.property()
    @Input internal val testConfig: Property<TestConfigInternal> = objects.property()
    @Input internal val uploadResults: RegularFileProperty = objects.fileProperty()
    @OutputDirectory val outputDir: Property<File> = objects.property()

    private val googleApi by lazy { GoogleApi(googleApiConfig.get()) }
    private val gcsBucketPath by lazy { "gs://${googleApi.bucketName}/${prefix.get()}" }
    private val gcsPaths by lazy { UploadResults.fromJson(uploadResults.get().asFile.readText()) }

    @TaskAction
    fun runTest() {
        val historyPicker = ToolResultsHistoryPicker(googleApi)
        val historyName = historyPicker.pickHistoryName(
            testConfig.get().resultsHistoryName.orNull,
            appPackageId.orNull)
        val historyId = historyPicker.getToolResultsHistoryId(historyName)

        val testMatrix = TestMatrix()
                .setClientInfo(clientInfo())
                .setResultStorage(resultStorage(historyId))
                .setEnvironmentMatrix(EnvironmentMatrix().setAndroidDeviceList(androidDeviceList()))
                .setTestSpecification(testConfig.get()
                    .testSpecification(
                        gcsPaths.appApk.asFileReference,
                        gcsPaths.testApk?.asFileReference,
                        gcsPaths.additionalApks.map { it.asFileReference },
                        gcsPaths.deviceFiles.map { it.asDeviceFileReference })
                    .get())

        log.info("Test matrix: ${testMatrix.toPrettyString()}")

        val triggeredTestMatrix = googleApi.testing.projects().testMatrices()
                .create(googleApi.projectId, testMatrix)
                .execute()

        log.info("Triggered matrix: ${triggeredTestMatrix.toPrettyString()}")

        val projectId = triggeredTestMatrix.projectId
        val matrixId = triggeredTestMatrix.testMatrixId

        val monitor = MatrixMonitor(googleApi, projectId, matrixId, testConfig.get().testType)

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

        if (testConfig.get().artifacts.isNotEmpty()) {
            with(ArtifactFetcherFactory(
                googleApi.storage.objects(),
                googleApi.bucketName,
                prefix.get(),
                outputDir.get()
            )) {
                for (test in supportedExecutions) {
                    val suffix = with(test.environment.androidDevice) {
                        "$androidModelId-$androidVersionId-$locale-$orientation"
                    }
                    log.lifecycle("Fetching result artifacts for $suffix...")
                    testConfig.get().artifacts.forEach { createFetcher(suffix, it).fetch() }
                }
            }
        }
    }

    private fun resultStorage(historyId: String?): ResultStorage = ResultStorage()
            .setGoogleCloudStorage(GoogleCloudStorage().setGcsPath(gcsBucketPath))
            .apply {
                if (historyId != null) {
                    toolResultsHistory = ToolResultsHistory()
                            .setProjectId(googleApi.projectId)
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
            .setAndroidDevices(testConfig.get().devices.get().map {
                AndroidDevice()
                    .setAndroidModelId(it.model)
                        .setAndroidVersionId(it.api.toString())
                        .setLocale(it.locale)
                        .setOrientation(it.orientation.name.toLowerCase())
            })
}

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
import com.simple.gradle.testlab.internal.AppFile
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.MatrixMonitor
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.ToolResultsHistoryPicker
import com.simple.gradle.testlab.internal.artifacts.ArtifactFetcherFactory
import com.simple.gradle.testlab.internal.createToolResultsUiUrl
import com.simple.gradle.testlab.internal.getToolResultsIds
import com.simple.gradle.testlab.internal.log
import com.simple.gradle.testlab.model.GoogleApiConfig
import com.simple.gradle.testlab.model.TestConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import java.util.Locale
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class TestLabTest @Inject constructor(
    layout: ProjectLayout,
    objects: ObjectFactory
) : DefaultTask() {
    @get:[Input Optional] val appPackageId: Property<String?> = objects.property()
    @get:Internal val googleApiConfig: Property<GoogleApiConfig> = objects.property()

    @get:Input val prefix: Property<String> = objects.property()
    @get:Input val clientDetails: MapProperty<String, String> = objects.mapProperty()
    @get:Nested val testConfig: Property<TestConfig> = objects.property()
    @get:InputFiles val appFileMetadata: ConfigurableFileCollection = objects.fileCollection()

    @get:OutputDirectory val outputDir: DirectoryProperty = objects.directoryProperty().apply {
        set(layout.buildDirectory.dir("test-results/$name"))
    }

    private val googleApi by lazy { GoogleApi(googleApiConfig.get(), logger) }
    private val gcsBucketPath by lazy { "gs://${googleApi.bucketName}/${prefix.get()}" }
    private val testConfigInternal: TestConfigInternal by lazy {
        testConfig.get() as TestConfigInternal
    }

    @TaskAction
    fun runTest() {
        val appFiles: List<AppFile> = appFileMetadata
            .map { AppFile.fromJson(it.readText()) }
            .reduce { acc, meta -> acc + meta }

        val historyPicker = ToolResultsHistoryPicker(googleApi)
        val historyName = historyPicker.pickHistoryName(
            testConfig.get().resultsHistoryName.orNull,
            appPackageId.orNull
        )
        val historyId = historyPicker.getToolResultsHistoryId(historyName)

        val testMatrix = TestMatrix()
            .setClientInfo(clientInfo())
            .setResultStorage(resultStorage(historyId))
            .setEnvironmentMatrix(EnvironmentMatrix().setAndroidDeviceList(androidDeviceList()))
            .setTestSpecification(testConfigInternal.testSpecification(appFiles))

        log.info("Test matrix: ${testMatrix.toPrettyString()}")

        val triggeredTestMatrix = googleApi.testing.projects().testMatrices()
            .create(googleApi.projectId, testMatrix)
            .execute()

        log.info("Triggered matrix: ${triggeredTestMatrix.toPrettyString()}")

        val projectId = triggeredTestMatrix.projectId
        val matrixId = triggeredTestMatrix.testMatrixId

        val monitor = MatrixMonitor(googleApi, projectId, matrixId, testConfigInternal.testType)

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

        if (testConfigInternal.artifacts.get().isNotEmpty()) {
            with(
                ArtifactFetcherFactory(
                    googleApi.storage,
                    googleApi.bucketName,
                    prefix.get(),
                    outputDir.get().asFile
                )
            ) {
                for (test in supportedExecutions) {
                    val suffix = buildString {
                        with(test.environment.androidDevice) {
                            append("$androidModelId-$androidVersionId-$locale-$orientation")
                        }
                        test.shard?.shardIndex?.let {
                            append("-shard_$it")
                        }
                    }
                    log.lifecycle("Fetching result artifacts for $suffix...")
                    testConfigInternal.artifacts.get().forEach { createFetcher(suffix, it).fetch() }
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

    private fun clientInfo(): ClientInfo = ClientInfo()
        .setName("gradle-test-lab-plugin")
        .setClientInfoDetails(
            clientDetails.orNull?.map { (key, value) -> ClientInfoDetail().setKey(key).setValue(value) }
        )

    private fun androidDeviceList(): AndroidDeviceList = AndroidDeviceList()
        .setAndroidDevices(
            testConfigInternal.devices.get().map {
                AndroidDevice()
                    .setAndroidModelId(it.model)
                    .setAndroidVersionId(it.api.toString())
                    .setLocale(it.locale)
                    .setOrientation(it.orientation.name.toLowerCase(Locale.ENGLISH))
            }
        )
}

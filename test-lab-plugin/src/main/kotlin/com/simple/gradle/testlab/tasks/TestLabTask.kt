package com.simple.gradle.testlab.tasks

import com.google.api.services.testing.model.AndroidDevice
import com.google.api.services.testing.model.AndroidDeviceList
import com.google.api.services.testing.model.ClientInfo
import com.google.api.services.testing.model.EnvironmentMatrix
import com.google.api.services.testing.model.GoogleCloudStorage
import com.google.api.services.testing.model.ResultStorage
import com.google.api.services.testing.model.TestMatrix
import com.simple.gradle.testlab.internal.ArtifactFetcher
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.MatrixMonitor
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.createToolResultsUiUrl
import com.simple.gradle.testlab.internal.getToolResultsIds
import com.simple.gradle.testlab.model.Artifacts
import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.GoogleApiConfig
import com.simple.gradle.testlab.model.TestConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

open class TestLabTask : DefaultTask() {
    @Input val appApk: Property<File> = project.objects.property(File::class.java)
    @Input val testApk: Property<File> = project.objects.property(File::class.java)
    @Input val google: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @Input val testConfig: Property<TestConfig> = project.objects.property(TestConfig::class.java)
    @Input val devices: ListProperty<Device> = project.objects.listProperty(Device::class.java)
    @Input @Optional val artifacts: Property<Artifacts> = project.objects.property(Artifacts::class.java)

    val outputDir: Property<File> = project.objects.property(File::class.java)

    @Internal lateinit var prefix: String
    @Internal lateinit var uploadResults: UploadResults

    init {
        group = "verification"
    }

    @get:Internal private val googleConfig by lazy { google.get() }
    @get:Internal private val googleApi by lazy { GoogleApi(googleConfig) }
    @get:Internal private val bucketName by lazy { googleConfig.bucketName ?: googleApi.defaultBucketName() }
    @get:Internal private val gcsBucketPath by lazy { "gs://$bucketName/$prefix" }

    @TaskAction
    fun runTest() {
        val appApkReference = uploadResults.references[appApk.get()]
                ?: throw GradleException("App APK not found: ${appApk.get()}")
        val testApkReference = uploadResults.references[testApk.get()]
                ?: throw GradleException("Test APK not found: ${testApk.get()}")

        val testMatrix = TestMatrix()
                .setClientInfo(clientInfo())
                .setResultStorage(resultStorage())
                .setEnvironmentMatrix(EnvironmentMatrix().setAndroidDeviceList(androidDeviceList()))
                .setTestSpecification(testConfig.get().testSpecification(appApkReference, testApkReference))

        logger.info("Test matrix: ${testMatrix.toPrettyString()}")

        val triggeredTestMatrix = googleApi.testing.projects().testMatrices()
                .create(googleConfig.projectId, testMatrix)
                .execute()

        val projectId = triggeredTestMatrix.projectId
        val matrixId = triggeredTestMatrix.testMatrixId

        val monitor = MatrixMonitor(googleApi, projectId, matrixId, testConfig.get().testType, logger)

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

        if (artifacts.isPresent) {
            val fetcher = ArtifactFetcher(project, googleApi, bucketName, prefix, outputDir.get(), logger)
            for (test in supportedExecutions) {
                fetcher.fetch(test.environment.androidDevice, artifacts.get())
            }
        }
    }

    private fun clientInfo(): ClientInfo = ClientInfo()
            .setName("Gradle Test Lab Plugin ${project.version}")

    private fun resultStorage(): ResultStorage = ResultStorage()
            .setGoogleCloudStorage(GoogleCloudStorage().setGcsPath(gcsBucketPath))

    private fun androidDeviceList(): AndroidDeviceList = AndroidDeviceList()
            .setAndroidDevices(devices.get().map {
                AndroidDevice()
                        .setAndroidModelId(it.model)
                        .setAndroidVersionId(it.version.toString())
                        .setLocale(it.locale)
                        .setOrientation(it.orientation.name.toLowerCase())
            })
}

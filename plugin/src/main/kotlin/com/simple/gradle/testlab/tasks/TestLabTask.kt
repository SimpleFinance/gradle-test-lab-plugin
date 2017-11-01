package com.simple.gradle.testlab.tasks

import com.google.testing.model.AndroidDevice
import com.google.testing.model.AndroidDeviceList
import com.google.testing.model.ClientInfo
import com.google.testing.model.EnvironmentMatrix
import com.google.testing.model.GoogleCloudStorage
import com.google.testing.model.ResultStorage
import com.google.testing.model.TestMatrix
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.GoogleApiConfig
import com.simple.gradle.testlab.model.TestConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class TestLabTask : DefaultTask() {
    @Input val appApk: Property<File> = project.objects.property(File::class.java)
    @Input val testApk: Property<File> = project.objects.property(File::class.java)
    @Input val google: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @Input val testConfig: Property<TestConfig> = project.objects.property(TestConfig::class.java)
    @Input val devices: ListProperty<Device> = project.objects.listProperty(Device::class.java)
    @Internal lateinit var prefix: String
    @Internal lateinit var uploadResults: UploadResults

    init {
        group = "verification"
    }

    @get:Internal val googleConfig by lazy { google.get() }

    @get:Internal val googleApi by lazy { GoogleApi(googleConfig) }

    @get:Internal val bucketName by lazy { googleConfig.bucketName ?: googleApi.defaultBucketName() }

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

        val triggeredTestMatrix = googleApi.testing.projects().testMatrices()
                .create(googleConfig.projectId, testMatrix)
                .execute()
    }

    private fun clientInfo(): ClientInfo = ClientInfo()
            .setName("Gradle Test Lab Plugin ${project.version}")

    private fun resultStorage(): ResultStorage = ResultStorage()
            .setGoogleCloudStorage(GoogleCloudStorage().setGcsPath("gs://$bucketName/$prefix"))

    private fun androidDeviceList(): AndroidDeviceList = AndroidDeviceList()
            .setAndroidDevices(devices.get().map {
                AndroidDevice()
                        .setAndroidModelId(it.model)
                        .setAndroidVersionId(it.version?.toString())
                        .setLocale(it.locale)
                        .setOrientation(it.orientation?.name?.toLowerCase())
            })
}

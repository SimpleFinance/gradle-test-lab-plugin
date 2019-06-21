package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.Apk
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.internal.artifacts.Artifact
import com.simple.gradle.testlab.model.Account
import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.DeviceFilesHandler
import com.simple.gradle.testlab.model.Orientation
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
internal abstract class AbstractTestConfig(
    override val testType: TestType,
    private val nameInternal: String,
    objects: ObjectFactory,
    private val providers: ProviderFactory
) : TestConfigInternal {
    override val account = DefaultAccount()
    override val artifacts = mutableSetOf<Artifact>()
    override val devices = objects.listProperty<Device>()
    override val files = objects.listProperty<DeviceFile>()

    override val additionalApks = objects.fileCollection()
    override val disablePerformanceMetrics = objects.property<Boolean>().convention(false)
    override val disableVideoRecording = objects.property<Boolean>().convention(false)
    override val resultsHistoryName = objects.property<String>()
    override val testTimeout = objects.property<String>().convention("900s")

    override val directoriesToPull = objects.listProperty<String>()
    override val networkProfile = objects.property<String>()

    override fun getName(): String = nameInternal

    override fun account(configure: Action<Account>) {
        configure.execute(account)
    }

    override fun device(
        model: String,
        api: Int,
        locale: String,
        orientation: Orientation
    ): Device = DefaultDevice(model, api, locale, orientation).also { devices.add(it) }

    override fun device(configure: Action<Device.Builder>) = providers.provider {
        Device.Builder().apply(configure::execute).build()
    }.also { devices.add(it) }

    override fun files(configure: Action<in DeviceFilesHandler>) =
        configure.execute(DefaultDeviceFilesHandler(files))

    override fun testSpecification(
        appApk: FileReference,
        testApk: FileReference?,
        additionalApks: List<FileReference>,
        deviceFiles: List<DeviceFileReference>
    ): Provider<TestSpecification> = providers.provider {
        TestSpecification()
            .setDisablePerformanceMetrics(disablePerformanceMetrics.get())
            .setDisableVideoRecording(disableVideoRecording.get())
            .setTestTimeout(testTimeout.get())
            .setTestSetup(testSetup(additionalApks, deviceFiles))
            .apply { configure(appApk, testApk) }
    }

    private fun testSetup(
        additionalApks: List<FileReference>,
        deviceFiles: List<DeviceFileReference>
    ): TestSetup = TestSetup()
        .setAdditionalApks(additionalApks.map { Apk().setLocation(it) })
        .setDirectoriesToPull(directoriesToPull.get())
        .setNetworkProfile(networkProfile.orNull)
        .setFilesToPush(deviceFiles.map { it.asDeviceFile })
        .setAccount(this@AbstractTestConfig.account.account)
        .apply { configure() }

    protected open fun TestSpecification.configure(
        appApk: FileReference,
        testApk: FileReference?
    ): TestSpecification = this

    protected open fun TestSetup.configure(): TestSetup = this
}

package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.Apk
import com.google.api.services.testing.model.ObbFile
import com.google.api.services.testing.model.RegularFile
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.internal.artifacts.Artifact
import com.simple.gradle.testlab.model.AccountHandler
import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.DeviceFilesHandler
import com.simple.gradle.testlab.model.FileType
import com.simple.gradle.testlab.model.Orientation
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import com.google.api.services.testing.model.DeviceFile as TestingDeviceFile

@Suppress("UnstableApiUsage")
internal abstract class AbstractTestConfig(
    override val testType: TestType,
    private val nameInternal: String,
    objects: ObjectFactory,
    private val providers: ProviderFactory
) : TestConfigInternal {
    override val account = objects.newInstance<DefaultAccountHandler>()
    override val artifacts = objects.setProperty<Artifact>().empty()
    override val devices = objects.listProperty<Device>()
    override val files = objects.listProperty<DeviceFile>().empty()

    override val additionalApks = objects.fileCollection()
    override val disablePerformanceMetrics = objects.property<Boolean>().value(false)
    override val disableVideoRecording = objects.property<Boolean>().value(false)
    override val resultsHistoryName = objects.property<String>()
    override val testTimeout = objects.property<String>().value("900s")

    override val directoriesToPull = objects.listProperty<String>().empty()
    override val networkProfile = objects.property<String>()

    override fun getName(): String = nameInternal

    override fun account(configure: Action<AccountHandler>) {
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

    override fun testSpecification(files: List<AppFile>): TestSpecification = TestSpecification()
        .setDisablePerformanceMetrics(disablePerformanceMetrics.get())
        .setDisableVideoRecording(disableVideoRecording.get())
        .setTestTimeout(testTimeout.get())
        .setTestSetup(testSetup(files))
        .apply { configure(files) }

    private fun testSetup(appFiles: List<AppFile>): TestSetup = TestSetup()
        .setAdditionalApks(appFiles.filter { it.type == FileType.EXTRA_APK }.map { Apk().setLocation(it.path) })
        .setDirectoriesToPull(directoriesToPull.get())
        .setNetworkProfile(networkProfile.orNull)
        .setFilesToPush(
            appFiles.filter { it.type == FileType.EXTRA_FILE }.map {
                TestingDeviceFile().setRegularFile(RegularFile().setContent(it.path))
            } + appFiles.filter { it.type == FileType.EXTRA_OBB }.map {
                TestingDeviceFile().setObbFile(ObbFile().setObb(it.path))
            }
        )
        .setAccount(this@AbstractTestConfig.account.account.get().testAccount)
        .apply { configure() }

    protected open fun TestSpecification.configure(files: List<AppFile>): TestSpecification = this

    protected open fun TestSetup.configure(): TestSetup = this
}

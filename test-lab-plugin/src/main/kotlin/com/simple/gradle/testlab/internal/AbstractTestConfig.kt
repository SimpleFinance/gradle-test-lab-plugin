package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.Account
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.GoogleAuto
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.Artifact
import com.simple.gradle.testlab.model.Device
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
    override val name: String,
    objects: ObjectFactory,
    private val providers: ProviderFactory
) : TestConfigInternal {
    override val artifacts = mutableSetOf<Artifact>()
    override val devices = objects.listProperty<Device>()

    override var disablePerformanceMetrics = objects.property<Boolean>().convention(false)
    override var disableVideoRecording = objects.property<Boolean>().convention(false)
    override var resultsHistoryName = objects.property<String>()
    override var testTimeout = objects.property<String>().convention("900s")

    override var autoGoogleAccount = objects.property<Boolean>().convention(true)
    override val directoriesToPull = objects.listProperty<String>()
    override var networkProfile = objects.property<String>()

    override fun device(
        model: String,
        api: Int,
        locale: String,
        orientation: Orientation
    ): Device = DefaultDevice(model, api, locale, orientation).also { devices.add(it) }

    override fun device(configure: Action<Device.Builder>) = providers.provider {
        Device.Builder().apply(configure::execute).build()
    }.also { devices.add(it) }

    override fun testSpecification(
        appApk: FileReference,
        testApk: FileReference?
    ): Provider<TestSpecification> = providers.provider {
        TestSpecification()
            .setDisablePerformanceMetrics(disablePerformanceMetrics.get())
            .setDisableVideoRecording(disableVideoRecording.get())
            .setTestTimeout(testTimeout.get())
            .setTestSetup(TestSetup()
                .setAccount(autoGoogleAccount.get().takeIf { it }
                    ?.let { Account().setGoogleAuto(GoogleAuto()) })
                .setDirectoriesToPull(directoriesToPull.get())
                .setNetworkProfile(networkProfile.orNull)
                .apply { configure() })
            .apply { configure(appApk, testApk) }
    }

    protected open fun TestSpecification.configure(
        appApk: FileReference,
        testApk: FileReference?
    ): TestSpecification = this

    protected open fun TestSetup.configure(): TestSetup  = this
}
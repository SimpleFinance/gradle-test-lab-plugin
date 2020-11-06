package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

/**
 * Base test configuration.
 *
 * @see InstrumentationTest
 * @see RoboTest
 */
@Suppress("UnstableApiUsage")
interface TestConfig : Named {
    /* The device will be logged in on this account for the duration of the test. */
    @get:Nested val account: AccountHandler

    /** APKs to install in addition to those being directly tested. Currently capped at 100. */
    @get:InputFiles val additionalApks: ConfigurableFileCollection

    /**
     * The java package for the test to be executed. The default value is determined by examining the
     * application's manifest.
     */
    @get:[Input Optional] val appPackageId: Property<String>

    /** Disables performance metrics recording; may reduce test latency. */
    @get:Input val disablePerformanceMetrics: Property<Boolean>

    /** Disables video recording; may reduce test latency. */
    @get:Input val disableVideoRecording: Property<Boolean>

    /**
     * Whether to prevent all runtime permissions to be granted at install time.
     */
    @get:Input val dontAutograntPermissions: Property<Boolean>

    /**
     * The name of the results history entry. This appears in the Firebase console and
     * identifies this test.
     */
    @get:[Input Optional] val resultsHistoryName: Property<String>

    /**
     * Max time a test execution is allowed to run before it is automatically cancelled.
     * Optional; the default is `5 min`.
     */
    @get:Input val testTimeout: Property<String>

    @get:[Input Optional] val systrace: SystraceHandler

    /**
     * List of directories on the device to upload to GCS at the end of the test; they must be
     * absolute paths under /sdcard or /data/local/tmp. Path names are restricted to characters a-z
     * A-Z 0-9 _ - . + and /
     *
     * Note: The paths /sdcard and /data will be made available and treated as implicit path
     * substitutions. E.g. if /sdcard on a particular device does not map to external storage, the
     * system will replace it with the external storage path prefix for that device.
     */
    @get:Input val directoriesToPull: ListProperty<String>

    /** The network traffic profile used for running the test. */
    @get:[Input Optional] val networkProfile: Property<String>

    /* The device will be logged in on this account for the duration of the test. */
    fun account(configure: Action<AccountHandler>)

    /** Configure and add a [device][Device] on which this test should run. */
    fun device(
        model: String = Device.DEFAULT.model,
        api: Int = Device.DEFAULT.api,
        locale: String = Device.DEFAULT.locale,
        orientation: Orientation = Device.DEFAULT.orientation
    ): Device

    /**
     * Configure and add a [device][Device] on which this test should run. This is for Groovy
     * script compatibility, and should not be used in Kotlin scripts.
     */
    fun device(configure: Action<Device.Builder>): Provider<Device>

    /** Configure the list of files to push to the device before starting the test. */
    fun files(configure: Action<in DeviceFilesHandler>)

    /** Configure systrace collection. */
    fun systrace(configure: Action<in SystraceHandler>)

    @Internal
    override fun getName(): String
}

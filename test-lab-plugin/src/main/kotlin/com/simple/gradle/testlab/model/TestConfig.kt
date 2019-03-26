package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Base test configuration.
 *
 * @see InstrumentationTest
 * @see RoboTest
 */
@Suppress("UnstableApiUsage")
interface TestConfig : Named {
    /* The device will be logged in on this account for the duration of the test. */
    val account: Account

    /** APKs to install in addition to those being directly tested. Currently capped at 100. */
    val additionalApks: ConfigurableFileCollection

    /** Disables performance metrics recording; may reduce test latency. */
    val disablePerformanceMetrics: Property<Boolean>

    /** Disables video recording; may reduce test latency. */
    val disableVideoRecording: Property<Boolean>

    /**
     * The name of the results history entry. This appears in the Firebase console and
     * identifies this test.
     */
    val resultsHistoryName: Property<String>

    /**
     * Max time a test execution is allowed to run before it is automatically cancelled.
     * Optional; the default is `5 min`.
     */
    val testTimeout: Property<String>

    /**
     * List of directories on the device to upload to GCS at the end of the test; they must be
     * absolute paths under /sdcard or /data/local/tmp. Path names are restricted to characters a-z
     * A-Z 0-9 _ - . + and /
     *
     * Note: The paths /sdcard and /data will be made available and treated as implicit path
     * substitutions. E.g. if /sdcard on a particular device does not map to external storage, the
     * system will replace it with the external storage path prefix for that device.
     */
    val directoriesToPull: ListProperty<String>

    /** The network traffic profile used for running the test. */
    val networkProfile: Property<String>

    /* The device will be logged in on this account for the duration of the test. */
    fun account(configure: Action<Account>)

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
}

package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.Named

/**
 * Base test configuration.
 *
 * @see InstrumentationTest
 * @see RoboTest
 */
interface TestConfig : Named {
    /**
     * The list of [Android devices][Device] on which this test will run. At least one device
     * is required.
     *
     * @see device
     */
    val devices: MutableList<out Device>

    /** The artifacts to fetch after this test is completed. */
    val artifacts: Artifacts

    /** Disables performance metrics recording; may reduce test latency. */
    var disablePerformanceMetrics: Boolean

    /** Disables video recording; may reduce test latency. */
    var disableVideoRecording: Boolean

    /**
     * The name of the results history entry. This appears in the Firebase console and
     * identifies this test.
     */
    var resultsHistoryName: String?

    /**
     * Max time a test execution is allowed to run before it is automatically cancelled.
     * Optional; the default is `5 min`.
     */
    var testTimeout: String

    /** Sign in to an automatically-created Google account for the duration of this test. */
    var autoGoogleAccount: Boolean

    /**
     * List of directories on the device to upload to GCS at the end of the test; they must be
     * absolute paths under /sdcard or /data/local/tmp. Path names are restricted to characters a-z
     * A-Z 0-9 _ - . + and /
     *
     * Note: The paths /sdcard and /data will be made available and treated as implicit path
     * substitutions. E.g. if /sdcard on a particular device does not map to external storage, the
     * system will replace it with the external storage path prefix for that device.
     */
    val directoriesToPull:  MutableList<String>

    /** Environment variables to set for the test (only applicable for instrumentation tests). */
    val environmentVariables: MutableMap<String, String>

    /** The network traffic profile used for running the test. */
    var networkProfile: String?

    /** Configure and add a [device][Device] on which this test should run. */
    fun device(configure: Closure<*>): Device

    /** Configure and add a [device][Device] on which this test should run. */
    fun device(configure: Device.() -> Unit): Device
}

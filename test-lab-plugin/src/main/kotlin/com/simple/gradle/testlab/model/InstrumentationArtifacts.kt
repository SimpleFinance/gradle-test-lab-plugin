package com.simple.gradle.testlab.model

/**
 * Artifacts to fetch after completing an instrumentation test. Results are stored in
 * `${task.outputDir}/$device` for each test device.
 */
interface InstrumentationArtifacts : Artifacts {
    /** Fetch instrumentation logs to `instrumentation.results`. */
    var instrumentation: Boolean

    /** Fetch JUnit test results to `test_result_$i.xml` for each result. */
    var junit: Boolean

    /** Fetch device logs to `logcat`. */
    var logcat: Boolean

    /** Fetch captured video to `video.mp4`. */
    var video: Boolean
}

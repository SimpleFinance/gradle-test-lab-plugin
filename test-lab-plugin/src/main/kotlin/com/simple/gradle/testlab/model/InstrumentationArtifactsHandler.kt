package com.simple.gradle.testlab.model

interface InstrumentationArtifactsHandler : ArtifactsHandler {
    /** Fetch instrumentation logs to `instrumentation.results`. */
    var instrumentation: Boolean

    /** Fetch JUnit test results to `test_result_$i.xml` for each result. */
    var junit: Boolean

    /** Fetch device logs to `logcat`. */
    var logcat: Boolean

    /** Fetch captured video to `video.mp4`. */
    var video: Boolean
}

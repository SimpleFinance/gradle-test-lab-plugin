package com.simple.gradle.testlab.model

enum class InstrumentationArtifact(internal vararg val artifacts: Artifact) {
    /** Fetch all available artifacts. */
    ALL(Artifact.INSTRUMENTATION, Artifact.JUNIT, Artifact.LOGCAT, Artifact.VIDEO),

    /** Fetch instrumentation logs to `instrumentation.results`. */
    INSTRUMENTATION(Artifact.INSTRUMENTATION),

    /** Fetch JUnit test results to `test_result_$i.xml` for each result. */
    JUNIT(Artifact.JUNIT),

    /** Fetch device logs to `logcat`. */
    LOGCAT(Artifact.LOGCAT),

    /** Fetch captured video to `video.mp4`. */
    VIDEO(Artifact.VIDEO)
}

package com.simple.gradle.testlab.model

enum class Artifact {
    /** Instrumentation logs. */
    INSTRUMENTATION,

    /** Instrumentation test results in JUnit XML format. */
    JUNIT,

    /** Device logs provided by the `logcat` tool. */
    LOGCAT,

    /** Screenshots taken during a robo test. */
    SCREENSHOTS,

    /** Video captured from the device during the test. */
    VIDEO
}

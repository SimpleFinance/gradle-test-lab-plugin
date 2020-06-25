package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.TestExecution
import com.google.api.services.testing.model.TestMatrix

internal enum class TestState(val terminal: Boolean, val error: Boolean, val description: String) {
    TEST_STATE_UNSPECIFIED(false, true, "Do not use.  For proto versioning only."),
    VALIDATING(false, false, "The execution or matrix is being validated."),
    PENDING(false, false, "The execution or matrix is waiting for resources to become available."),
    RUNNING(false, false, "The execution is currently being processed."),
    FINISHED(true, false, "The execution or matrix has terminated normally."),
    ERROR(true, false, "The execution or matrix has stopped because it encountered an infrastructure failure."),
    UNSUPPORTED_ENVIRONMENT(true, true, "The execution was not run because it corresponds to a unsupported environment."),
    INCOMPATIBLE_ENVIRONMENT(true, true, "The execution was not run because the provided inputs are incompatible with the requested environment.\n\nExample: requested AndroidVersion is lower than APK's minSdkVersion"),
    INCOMPATIBLE_ARCHITECTURE(true, true, "The execution was not run because the provided inputs are incompatible with the requested architecture.\n\nExample: requested device does not support running the native code in\nthe supplied APK"),
    CANCELLED(true, false, "The user cancelled the execution."),
    INVALID(true, true, "The execution or matrix was not run because the provided inputs are not valid.\n\nExamples: input file is not of the expected type, is malformed/corrupt, or was flagged as malware")
}

internal val TestExecution.testState: TestState get() = TestState.valueOf(state)
internal val TestMatrix.testState: TestState get() = TestState.valueOf(state)

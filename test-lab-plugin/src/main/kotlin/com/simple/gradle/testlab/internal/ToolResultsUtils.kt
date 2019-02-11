package com.simple.gradle.testlab.internal

import com.google.api.client.http.UriTemplate
import com.google.api.services.testing.model.TestMatrix
import org.gradle.api.GradleException

internal data class ToolResultsIds(val historyId: String, val executionId: String)

internal enum class InvalidMatrixDetails(val message: String) {
    MALFORMED_APK("The app APK is not a valid Android application"),
    MALFORMED_TEST_APK("The test APK is not a valid Android instrumentation test"),
    NO_MANIFEST("The app APK is missing the manifest file"),
    NO_PACKAGE_NAME("The APK manifest file is missing the package name"),
    TEST_SAME_AS_APP("The test APK is the same as the app APK"),
    NO_INSTRUMENTATION("The test APK declares no instrumentation tags in the manifest"),
    INSTRUMENTATION_ORCHESTRATOR_INCOMPATIBLE("""
        The test runner class specified by the user or the test APK's
        manifest file is not compatible with Android Test Orchestrator.
        Please use AndroidJUnitRunner version 1.0 or higher
    """.trimIndent()),
    NO_TEST_RUNNER_CLASS("""
        The test APK does not contain the test runner class specified by
        the user or the manifest file. The test runner class name may be
        incorrect, or the class may be mislocated in the app APK.
    """.trimIndent()),
    NO_LAUNCHER_ACTIVITY("The app APK does not specify a main launcher activity"),
    FORBIDDEN_PERMISSIONS("The app declares one or more permissions that are not allowed"),
    INVALID_ROBO_DIRECTIVES("Cannot have multiple robo-directives with the same resource name"),
    TEST_LOOP_INTENT_FILTER_NOT_FOUND("The app does not have a correctly formatted game-loop intent filter"),
    SCENARIO_LABEL_NOT_DECLARED("A scenario-label was not declared in the manifest file"),
    SCENARIO_LABEL_MALFORMED("A scenario-label in the manifest includes invalid numbers or ranges"),
    SCENARIO_NOT_DECLARED("A scenario-number was not declared in the manifest file"),
    TEST_ONLY_APK("\"testOnly\" found in the Manifest. testOnly APKs are not allowed"),
    NO_SIGNATURE("There is a problem with signature of at least one of the supplied APK(s)")
}

internal fun createToolResultsUiUrl(projectId: String, toolResultsIds: ToolResultsIds) =
        UriTemplate.expand(
                "https://console.firebase.google.com/",
                "/project/{project}/testlab/histories/{history}/matrices/{execution}",
                linkedMapOf(
                        "project" to projectId,
                        "history" to toolResultsIds.historyId,
                        "execution" to toolResultsIds.executionId),
                false)

internal fun getToolResultsIds(
    matrix: TestMatrix,
    monitor: MatrixMonitor,
    statusInterval: Long = 10000L
): ToolResultsIds {
    var status = matrix
    while (true) {
        status.resultStorage.toolResultsExecution?.run {
            if (historyId != null && executionId != null) {
                return ToolResultsIds(historyId, executionId)
            }
        }

        if (status.testState in MatrixMonitor.completedMatrixStates) {
            throw GradleException(formatInvalidMatrixError(matrix))
        }

        Thread.sleep(statusInterval)
        status = monitor.getTestMatrixStatus()
    }
}

private fun formatInvalidMatrixError(matrix: TestMatrix): String {
    val details = try {
        InvalidMatrixDetails.valueOf(matrix.invalidMatrixDetails)
    } catch (e: Exception) {
        null
    }

    if (details != null) {
        return "Matrix [${matrix.testMatrixId}] failed during validation: ${details.message}"
    }

    return """
        Matrix [${matrix.testMatrixId}] unexpectedly reached final status ${matrix.state} without returning
        a URL to any test results in the Firebase console. Please re-check the
        validity of your APK file(s) and test parameters and try again.
    """.trimIndent()
}

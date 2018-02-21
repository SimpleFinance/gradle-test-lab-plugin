package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.CancelTestMatrixResponse
import com.google.api.services.testing.model.Environment
import com.google.api.services.testing.model.TestExecution
import com.google.api.services.testing.model.TestMatrix
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class MatrixMonitor(
        val googleApi: GoogleApiInternal,
        val projectId: String,
        val matrixId: String,
        val testType: TestType,
        val logger: Logger
) {
    companion object {
        private const val statusInterval = 10000L
    }

    val completedExecutionStates = setOf(
            TestState.FINISHED,
            TestState.ERROR,
            TestState.UNSUPPORTED_ENVIRONMENT,
            TestState.INCOMPATIBLE_ENVIRONMENT,
            TestState.INCOMPATIBLE_ARCHITECTURE,
            TestState.CANCELLED,
            TestState.INVALID)

    val completedMatrixStates = setOf(
            TestState.FINISHED,
            TestState.ERROR,
            TestState.CANCELLED,
            TestState.INVALID)

    private var maxStatusLength = 0

    fun handleUnsupportedExecutions(matrix: TestMatrix): List<TestExecution> {
        val supportedTests = mutableListOf<TestExecution>()
        val unsupportedDimensions = mutableSetOf<String>()

        for (test in matrix.testExecutions) {
            if (test.testState == TestState.UNSUPPORTED_ENVIRONMENT) {
                unsupportedDimensions.add(formatInvalidDimension(test.environment))
            } else {
                supportedTests.add(test)
            }
        }

        if (unsupportedDimensions.isNotEmpty()) {
            logger.warn("Some device dimensions are not compatible and will be skipped:\n  "
                    + unsupportedDimensions.joinToString("\n  "))
        }

        val type = testType.name.toLowerCase()
        logger.lifecycle("Firebase Test Lab will execute your $type test on ${supportedTests.size} devices.")

        return supportedTests
    }

    fun getTestMatrixStatus(): TestMatrix =
            googleApi.testing.projects().testMatrices().get(projectId, matrixId).execute()

    fun cancelTestMatrix(): CancelTestMatrixResponse =
            googleApi.testing.projects().testMatrices().cancel(projectId, matrixId).execute()

    fun monitorTestExecutionProgress(testId: String) {
        var lastState: TestState? = null
        var error: String? = null
        var progress: List<String> = emptyList()
        var lastProgressSize = 0

        while (true) {
            val status = getTestExecutionStatus(testId)
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)

            status.testDetails?.run {
                error = errorMessage
                progress = progressMessages ?: emptyList()
            }

            for (msg in progress.takeLast(progress.size - lastProgressSize)) {
                logger.lifecycle("$timestamp ${msg.trimEnd()}")
            }
            lastProgressSize = progress.size

            if (status.testState == TestState.ERROR) {
                throw GradleException("Infrastructure error: $error")
            }

            if (status.testState == TestState.UNSUPPORTED_ENVIRONMENT) {
                val dimension = formatInvalidDimension(status.environment)
                throw GradleException("""Device dimensions are not compatible: $dimension""")
            }

            if (status.testState != lastState) {
                lastState = status.testState
                logger.lifecycle("$timestamp Test is ${lastState.name.toLowerCase()}")
            }

            if (status.testState in completedExecutionStates) break

            Thread.sleep(statusInterval)
        }

        var matrix = getTestMatrixStatus()
        while (matrix.testState !in completedMatrixStates) {
            logger.debug("Matrix not yet complete, still in state: ${matrix.testState}")
            Thread.sleep(statusInterval)
            matrix = getTestMatrixStatus()
        }

        logTestComplete(matrix.testState)
    }

    fun monitorTestMatrixProgress() {
        while (true) {
            val matrix = getTestMatrixStatus()

            val stateCounts = mutableMapOf<TestState, Int>()
            matrix.testExecutions
                    .groupingBy { it.testState }
                    .eachCountTo(stateCounts)

            updateMatrixStatus(stateCounts)

            if (matrix.testState in completedMatrixStates) {
                logTestComplete(matrix.testState)
                break
            }

            Thread.sleep(statusInterval)
        }
    }

    private fun updateMatrixStatus(stateCounts: MutableMap<TestState, Int>) {
        val status = mutableListOf<String>()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        stateCounts.forEach { (state, count) ->
            if (count > 0) status.add("$state:$count")
        }
        status.sort()
        val out = "\r$timestamp Test matrix status: ${status.joinToString(" ")} "
        maxStatusLength = Math.max(out.length, maxStatusLength)
        logger.lifecycle(out.padEnd(maxStatusLength, ' '))
    }

    private fun getTestExecutionStatus(testId: String) =
        getTestMatrixStatus().testExecutions.firstOrNull { it.id == testId }
                ?: throw GradleException("Test execution not found: matrix $matrixId, test $testId")

    private fun logTestComplete(state: TestState) = with (logger) {
        info("Test matrix completed in state: $state")
        lifecycle("${testType.name.toLowerCase().capitalize()} testing complete.")
    }

    private fun formatInvalidDimension(environment: Environment): String =
        environment.androidDevice?.run {
            "[OS-version $androidVersionId on $androidModelId]"
        } ?: "[unknown-environment]"
}

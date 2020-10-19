package com.simple.gradle.testlab

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.io.File

class IntegrationTest {

    private val rootProjectDir = File("..").canonicalFile!!
    private val customInstallationBuildDir = File(rootProjectDir, "build/custom")
    private val customInstallation by lazy {
        customInstallationBuildDir.listFiles()?.let {
            it.singleOrNull { it.name.startsWith("gradle") }
                ?: throw IllegalStateException(
                    "Expected 1 custom installation but found ${it.size}. Run `./gradlew clean customInstallation`."
                )
        } ?: throw IllegalStateException("Custom installation not found. Run `./gradlew customInstallation`.")
    }

    @Test
    fun `groovy integration test`() {
        val result = GradleRunner.create()
            // .withDebug(true) -- https://github.com/gradle/gradle/issues/6862
            .withGradleInstallation(customInstallation)
            .withProjectDir(File("../sample"))
            .withArguments(
                ":clean", ":tasks",
                "--all",
                "--warning-mode", "all",
                "--stacktrace",
                "--include-build=$rootProjectDir"
            )
            .build()
        expectThat(result.task(":tasks")?.outcome).isEqualTo(SUCCESS)
        expectThat(result.output).and {
            contains("testLabDebugFooTest")
            contains("testLabDebugFooUploadFiles")
            contains("testLabDebugInstrumentedTest")
            contains("testLabDebugInstrumentedUploadFiles")
            contains("testLabReleaseFooTest")
            contains("testLabReleaseFooUploadFiles")
        }
    }

    @Test
    fun `kotlin integration test`() {
        val result = GradleRunner.create()
            // .withDebug(true) -- https://github.com/gradle/gradle/issues/6862
            .withGradleInstallation(customInstallation)
            .withProjectDir(File("../sample-kotlin"))
            .withArguments(
                ":clean", ":tasks",
                "--all",
                "--warning-mode", "all",
                "--stacktrace",
                "--include-build=$rootProjectDir"
            )
            .build()

        expectThat(result.output).and {
            contains("testLabDebugFooTest")
            contains("testLabDebugFooUploadFiles")
            contains("testLabDebugInstrumentedTest")
            contains("testLabDebugInstrumentedUploadFiles")
            contains("testLabReleaseFooTest")
            contains("testLabReleaseFooUploadFiles")
        }
    }
}

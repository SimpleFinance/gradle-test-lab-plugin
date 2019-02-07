package com.simple.gradle.testlab

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Test
import java.io.File

class IntegrationTest {

    private val rootProjectDir = File("..").canonicalFile!!
    private val customInstallationBuildDir = File(rootProjectDir, "build/custom")
    private val customInstallation by lazy {
        customInstallationBuildDir.listFiles()?.let {
            it.singleOrNull { it.name.startsWith("gradle") }
                ?: throw IllegalStateException(
                    "Expected 1 custom installation but found ${it.size}. Run `./gradlew clean customInstallation`.")
        } ?: throw IllegalStateException("Custom installation not found. Run `./gradlew customInstallation`.")
    }

    @Test
    fun `groovy integration test`() {
        val result = GradleRunner.create()
            // .withDebug(true) -- https://github.com/gradle/gradle/issues/6862
            .withGradleInstallation(customInstallation)
            .withProjectDir(File("../sample"))
            .withArguments(":clean", ":tasks", "--all", "--stacktrace", "--include-build=$rootProjectDir")
            .build()
        assertThat(result.task(":tasks")?.outcome, equalTo(SUCCESS))
        assertThat(result.output, containsSubstring("testLabUploadDebugAppApk"))
        assertThat(result.output, containsSubstring("testLabUploadDebugTestApk"))
        assertThat(result.output, containsSubstring("testLabUploadReleaseAppApk"))
        assertThat(result.output, containsSubstring("testLabReleaseFooTest"))
        assertThat(result.output, containsSubstring("testLabDebugFooTest"))
        assertThat(result.output, containsSubstring("testLabDebugInstrumentedTest"))
    }

    @Test
    fun `kotlin integration test`() {
        val result = GradleRunner.create()
            // .withDebug(true) -- https://github.com/gradle/gradle/issues/6862
            .withGradleInstallation(customInstallation)
            .withProjectDir(File("../sample-kotlin"))
            .withArguments(":clean", ":tasks", "--all", "--stacktrace", "--include-build=$rootProjectDir")
            .build()

        assertThat(result.task(":tasks")?.outcome, equalTo(SUCCESS))
        assertThat(result.output, containsSubstring("testLabUploadDebugAppApk"))
        assertThat(result.output, containsSubstring("testLabUploadDebugTestApk"))
        assertThat(result.output, containsSubstring("testLabUploadReleaseAppApk"))
        assertThat(result.output, containsSubstring("testLabReleaseFooTest"))
        assertThat(result.output, containsSubstring("testLabDebugFooTest"))
        assertThat(result.output, containsSubstring("testLabDebugInstrumentedTest"))
    }
}

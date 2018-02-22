package com.simple.gradle.testlab

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Ignore
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
            .withDebug(true)
            .withGradleInstallation(customInstallation)
            .withProjectDir(File("../sample"))
            .withArguments(":clean", ":tasks", "--all", "--stacktrace", "--include-build=$rootProjectDir")
            .build()
        assert.that(result.task(":tasks")?.outcome, equalTo(SUCCESS))
        assert.that(result.output, containsSubstring("testLabUploadDebugAppApk"))
        assert.that(result.output, containsSubstring("testLabUploadDebugTestApk"))
        assert.that(result.output, containsSubstring("testLabUploadReleaseAppApk"))
        assert.that(result.output, containsSubstring("testLabReleaseFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugInstrumentedTest"))
    }

    @Test
    @Ignore("The sample works, but this doesn't. Figure out why.")
    fun `kotlin integration test`() {
        val result = GradleRunner.create()
            .withDebug(true)
            .withGradleInstallation(customInstallation)
            .withProjectDir(File("../sample-kotlin"))
            .withArguments(":clean", ":tasks", "--all", "--stacktrace", "--include-build=$rootProjectDir")
            .build()

        assert.that(result.task(":tasks")?.outcome, equalTo(SUCCESS))
        assert.that(result.output, containsSubstring("testLabUploadDebugAppApk"))
        assert.that(result.output, containsSubstring("testLabUploadDebugTestApk"))
        assert.that(result.output, containsSubstring("testLabUploadReleaseAppApk"))
        assert.that(result.output, containsSubstring("testLabReleaseFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugInstrumentedTest"))
    }
}

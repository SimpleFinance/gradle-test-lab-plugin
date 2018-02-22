package com.simple.gradle.testlab

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class IntegrationTest {
    @get:Rule val projectDir = TemporaryFolder()

    @Test
    fun `groovy integration test`() {
        File("../sample").copyRecursively(projectDir.root)
        val result = GradleRunner.create()
            .withDebug(true)
            .withProjectDir(projectDir.root)
            .withArguments(":tasks", "--all", "--stacktrace")
            .withPluginClasspath()
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
        File("../sample-kotlin").copyRecursively(projectDir.root)
        val result = GradleRunner.create()
            .withDebug(true)
            .withProjectDir(projectDir.root)
            .withArguments(":tasks", "--all", "--stacktrace")
            .withPluginClasspath()
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

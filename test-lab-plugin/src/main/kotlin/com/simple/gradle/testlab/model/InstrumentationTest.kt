package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

/**
 * A test of an Android application that can control an Android component independently of its
 * normal lifecycle. Android instrumentation tests run an application APK and test APK inside the
 * same process on a virtual or physical AndroidDevice. They also specify a test runner class, such
 * as `AndroidJUnitTestRunner`, which can vary on the specific instrumentation framework chosen.
 */
@Suppress("UnstableApiUsage")
interface InstrumentationTest : TestConfig {
    /** Environment variables to set for the test (only applicable for instrumentation tests). */
    @get:Input val environmentVariables: MapProperty<String, String>

    /**
     * The `InstrumentationTestRunner` class. Optional; the default is determined by examining the
     * application's manifest.
     */
    @get:[Input Optional] val testRunnerClass: Property<String>

    /**
     * Test targets to execute. Optional; if empty, all targets in the module will
     * be ran.
     */
    @Deprecated(
        message = "Replaced with targets function",
        replaceWith = ReplaceWith("targets { }"),
        level = DeprecationLevel.WARNING
    )
    @get:Input val testTargets: ListProperty<String>

    /**
     * Test targets to execute. If empty, all targets in the module will be ran.
     */
    @get:Nested val targets: TestTargetsHandler

    /**
     * Run each test within its own invocation with the
     * [Android Test Orchestrator](https://developer.android.com/training/testing/junit-runner.html#using-android-test-orchestrator).
     *
     * The orchestrator is only compatible with `AndroidJUnitRunner` 1.0 or higher.
     *
     * Using the orchestrator provides the following benefits:
     * - No shared state
     * - Crashes are isolated
     * - Logs are scoped per test
     *
     * Optional; if empty, the test will run without the orchestrator.
     */
    @get:[Input Optional] val useOrchestrator: Property<Boolean>

    /**
     * Configures [artifacts][InstrumentationArtifactsHandler] to fetch after completing the test.
     */
    fun artifacts(configure: Action<in InstrumentationArtifactsHandler>)

    /**
     * Configure test targets and shards to execute.
     *
     * See [https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#execution-options] for the
     * syntax of [targets].
     *
     * Typical targets are of the following forms:
     *
     * - `package name.of.package`
     * - `class fully.qualified.ClassName`
     * - `class fully.qualified.ClassName#MethodName`
     *
     * @see TestTargets
     */
    fun targets(configure: Action<in TestTargetsHandler>)

    /**
     * Adds a package target.
     *
     * @param packageName the fully-qualified package name
     */
    @Deprecated(
        message = "Replaced with targets function",
        replaceWith = ReplaceWith("targets { packages.add(packageName) }"),
        level = DeprecationLevel.WARNING
    )
    fun targetPackage(packageName: String)

    /**
     * Adds a class target.
     *
     * @param className the fully-qualified class name
     */
    @Deprecated(
        message = "Replaced with targets function",
        replaceWith = ReplaceWith("targets { classes.add(className) }"),
        level = DeprecationLevel.WARNING
    )
    fun targetClass(className: String)

    /**
     * Adds a method target.
     *
     * @param className the fully-qualified class name
     * @param methodName the method to execute
     */
    @Deprecated(
        message = "Replaced with targets function",
        replaceWith = ReplaceWith("targets { classes.add(className + \"#\" + methodName) }"),
        level = DeprecationLevel.WARNING
    )
    fun targetMethod(className: String, methodName: String)
}

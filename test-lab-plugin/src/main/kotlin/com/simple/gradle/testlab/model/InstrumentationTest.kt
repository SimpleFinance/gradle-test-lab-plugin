package com.simple.gradle.testlab.model

import groovy.lang.Closure

/**
 * A test of an Android application that can control an Android component independently of its
 * normal lifecycle. Android instrumentation tests run an application APK and test APK inside the
 * same process on a virtual or physical AndroidDevice. They also specify a test runner class, such
 * as `AndroidJUnitTestRunner`, which can vary on the specific instrumentation framework chosen.
 */
interface InstrumentationTest : TestConfig {
    /**
     * The [artifacts][InstrumentationArtifacts] to fetch after completing the test.
     *
     * @see artifacts
     */
    override val artifacts: InstrumentationArtifacts

    /**
     * The `InstrumentationTestRunner` class. Optional; the default is determined by examining the
     * application's manifest.
     */
    var testRunnerClass: String?

    /**
     * [Test targets][TestTargets] to execute. Optional; if empty, all targets in the module will
     * be ran.
     *
     * @see targets
     */
    val testTargets: TestTargets

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
    var useOrchestrator: Boolean?

    /** Configure the [artifacts][InstrumentationArtifacts] to fetch after the test has completed. */
    fun artifacts(configure: Closure<*>): InstrumentationArtifacts

    /** Configure the artifacts to fetch after the test has completed. */
    fun artifacts(configure: InstrumentationArtifacts.() -> Unit): InstrumentationArtifacts

    /** Configure the [test targets][TestTargets] to execute. */
    fun targets(configure: Closure<*>): TestTargets

    /** Configure the [test targets][TestTargets] to execute. */
    fun targets(configure: TestTargets.() -> Unit): TestTargets
}

package com.simple.gradle.testlab.model

import org.gradle.api.Action

interface InstrumentationTest : TestConfig {
    var testRunnerClass: String?
    var useOrchestrator: Boolean?
    val testTargets: TestTargets

    fun targets(configure: Action<in TestTargets>)
}

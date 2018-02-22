package com.simple.gradle.testlab.model

import groovy.lang.Closure

interface InstrumentationTest : TestConfig {
    var testRunnerClass: String?
    var useOrchestrator: Boolean?
    val testTargets: TestTargets

    fun targets(configure: Closure<*>): TestTargets
    fun targets(configure: TestTargets.() -> Unit): TestTargets
}

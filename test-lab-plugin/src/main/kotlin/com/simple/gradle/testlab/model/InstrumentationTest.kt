package com.simple.gradle.testlab.model

import groovy.lang.Closure

interface InstrumentationTest : TestConfig {
    override val artifacts: InstrumentationArtifacts
    var testRunnerClass: String?
    var useOrchestrator: Boolean?
    val testTargets: TestTargets

    fun artifacts(configure: Closure<*>): InstrumentationArtifacts
    fun artifacts(configure: InstrumentationArtifacts.() -> Unit): InstrumentationArtifacts
    fun targets(configure: Closure<*>): TestTargets
    fun targets(configure: TestTargets.() -> Unit): TestTargets
}

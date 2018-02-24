package com.simple.gradle.testlab.model

import groovy.lang.Closure

interface RoboTest : TestConfig {
    var appInitialActivity: String?
    override val artifacts: RoboArtifacts
    var maxDepth: Int?
    var maxSteps: Int?
    val roboDirectives: RoboDirectives

    fun artifacts(configure: Closure<*>): RoboArtifacts
    fun artifacts(configure: RoboArtifacts.() -> Unit): RoboArtifacts
    fun roboDirectives(configure: Closure<*>): RoboDirectives
    fun roboDirectives(configure: RoboDirectives.() -> Unit): RoboDirectives
}

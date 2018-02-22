package com.simple.gradle.testlab.model

import groovy.lang.Closure

interface RoboTest : TestConfig {
    var appInitialActivity: String?
    var maxDepth: Int?
    var maxSteps: Int?
    val roboDirectives: RoboDirectives

    fun roboDirectives(configure: Closure<*>): RoboDirectives
    fun roboDirectives(configure: RoboDirectives.() -> Unit): RoboDirectives
}

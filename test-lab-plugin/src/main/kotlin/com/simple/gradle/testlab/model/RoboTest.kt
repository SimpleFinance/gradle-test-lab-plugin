package com.simple.gradle.testlab.model

import org.gradle.api.Action

interface RoboTest : TestConfig {
    var appInitialActivity: String?
    var maxDepth: Int?
    var maxSteps: Int?
    val roboDirectives: RoboDirectives

    fun roboDirectives(configure: Action<in RoboDirectives>)
}

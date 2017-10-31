package com.simple.gradle.testlab.model

import com.google.testing.model.AndroidRoboTest
import com.google.testing.model.RoboDirective
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil

class RoboTestBuilder {
    var appInitialActivity: String? = null
    var maxDepth: Int? = null
    var maxSteps: Int? = null

    private val roboDirectives = RoboDirectivesBuilder()

    fun roboDirectives(configureClosure: Closure<*>) =
            ConfigureUtil.configure(configureClosure, roboDirectives)

    fun roboDirectives(configure: RoboDirectivesBuilder.() -> Unit) =
            roboDirectives.configure()

    internal fun build(): AndroidRoboTest = AndroidRoboTest()
            .setAppInitialActivity(appInitialActivity)
            .setMaxDepth(maxDepth)
            .setMaxSteps(maxSteps)
            .setRoboDirectives(roboDirectives.build())
}

class RoboDirectivesBuilder {
    private val directives = mutableListOf<RoboDirectiveBuilder>()

    fun click(resourceName: String) =
            directives.add(RoboDirectiveBuilder("click", resourceName))

    fun text(resourceName: String, inputText: String) =
            directives.add(RoboDirectiveBuilder("text", resourceName, inputText))

    fun build(): List<RoboDirective> = directives.toList().map { it.build() }
}

internal class RoboDirectiveBuilder(val actionType: String, val resourceName: String, val inputText: String? = null) {
    internal fun build(): RoboDirective = RoboDirective()
            .setActionType(actionType)
            .setResourceName(resourceName)
            .setInputText(inputText)
}

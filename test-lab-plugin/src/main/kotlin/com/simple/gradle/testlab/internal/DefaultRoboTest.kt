package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidRoboTest
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.RoboDirective
import com.simple.gradle.testlab.model.RoboDirectives
import com.simple.gradle.testlab.model.RoboTest
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil
import javax.inject.Inject
import com.google.api.services.testing.model.RoboDirective as GoogleRoboDirective

open class DefaultRoboTest @Inject constructor(name: String = "robo")
    : AbstractTestConfig(name, TestType.ROBO), RoboTest {

    override var appInitialActivity: String? = null
    override var maxDepth: Int? = null
    override var maxSteps: Int? = null
    override val roboDirectives: RoboDirectives = DefaultRoboDirectives()

    override val requiresTestApk: Boolean = false

    override fun roboDirectives(configure: Closure<*>): RoboDirectives =
        roboDirectives.apply { ConfigureUtil.configure(configure, this) }

    override fun roboDirectives(configure: RoboDirectives.() -> Unit): RoboDirectives =
        roboDirectives.apply(configure)

    override fun buildTestSpecification(appApk: FileReference, testApk: FileReference?): TestSpecification =
            TestSpecification().setAndroidRoboTest(
                    AndroidRoboTest()
                            .setAppApk(appApk)
                            .setAppInitialActivity(appInitialActivity)
                            .setMaxDepth(maxDepth)
                            .setMaxSteps(maxSteps)
                            .setRoboDirectives(roboDirectives.toDomain()))

    private fun RoboDirectives.toDomain() =
        directives.map { it.toDomain() }

    private fun RoboDirective.toDomain() =
        GoogleRoboDirective()
            .setActionType(actionType)
            .setResourceName(resourceName)
            .setInputText(inputText)
}

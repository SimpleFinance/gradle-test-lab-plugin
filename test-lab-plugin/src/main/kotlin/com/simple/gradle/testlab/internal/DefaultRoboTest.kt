package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidRoboTest
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.RoboDirective
import com.simple.gradle.testlab.model.RoboDirectives
import com.simple.gradle.testlab.model.RoboTest
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.internal.reflect.Instantiator
import javax.inject.Inject
import com.google.api.services.testing.model.RoboDirective as GoogleRoboDirective

open class DefaultRoboTest @Inject constructor(name: String = "robo")
    : AbstractTestConfig(name, TestType.ROBO), RoboTest {

    override var appInitialActivity: String? = null
    override var maxDepth: Int? = null
    override var maxSteps: Int? = null
    override val roboDirectives: RoboDirectives = DefaultRoboDirectives()

    override val requiresTestApk: Boolean = false

    override fun roboDirectives(configure: Action<in RoboDirectives>) {
        configure.execute(roboDirectives)
    }

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

package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidRoboTest
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.RoboArtifactsHandler
import com.simple.gradle.testlab.model.RoboDirective
import com.simple.gradle.testlab.model.RoboDirectivesHandler
import com.simple.gradle.testlab.model.RoboTest
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject
import com.google.api.services.testing.model.RoboDirective as GoogleRoboDirective

@Suppress("UnstableApiUsage")
internal open class DefaultRoboTest @Inject constructor(
    objects: ObjectFactory,
    private val providers: ProviderFactory,
    name: String
) : AbstractTestConfig(name, TestType.ROBO, objects, providers), RoboTest {

    private val artifactsHandler by lazy {
        DefaultRoboArtifactsHandler(artifacts)
    }

    private val roboDirectivesHandler by lazy {
        DefaultRoboDirectivesHandler(directives)
    }

    override val appInitialActivity = objects.property<String>()
    override val directives = objects.listProperty<RoboDirective>()
    override val maxDepth = objects.property<Int>()
    override val maxSteps = objects.property<Int>()

    override val requiresTestApk: Boolean = false

    override fun artifacts(configure: Action<in RoboArtifactsHandler>) =
        configure.execute(artifactsHandler)

    override fun directives(configure: Action<in RoboDirectivesHandler>) =
        configure.execute(roboDirectivesHandler)

    override fun buildTestSpecification(
        appApk: FileReference,
        testApk: FileReference?
    ): Provider<TestSpecification> {
        return providers.provider {
            TestSpecification().setAndroidRoboTest(
                AndroidRoboTest()
                    .setAppApk(appApk)
                    .setAppInitialActivity(appInitialActivity.orNull)
                    .setMaxDepth(maxDepth.orNull)
                    .setMaxSteps(maxSteps.orNull)
                    .setRoboDirectives(directives.get().map { it.toDomain() }))
        }
    }
}

private fun RoboDirective.toDomain() =
    GoogleRoboDirective()
        .setActionType(actionType)
        .setResourceName(resourceName)
        .setInputText(inputText)

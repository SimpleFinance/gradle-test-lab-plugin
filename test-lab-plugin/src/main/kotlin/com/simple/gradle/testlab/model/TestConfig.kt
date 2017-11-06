package com.simple.gradle.testlab.model

import com.google.api.services.testing.model.AndroidInstrumentationTest
import com.google.api.services.testing.model.AndroidRoboTest
import com.google.api.services.testing.model.EnvironmentVariable
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.RoboDirective
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil

enum class TestType { INSTRUMENTATION, ROBO }

abstract class TestConfig(val testType: TestType) {
    var autoGoogleLogin: Boolean = false
    var disablePerformanceMetrics: Boolean = false
    var disableVideoRecording: Boolean = false
    var testTimeout: String? = null

    // For TestSetup
    val directoriesToPull = mutableListOf<String>()
    val environmentVariables = mutableMapOf<String, String>()
    // TODO filesToPush
    var networkProfile: String? = null

    fun testSpecification(appApk: FileReference, testApk: FileReference): TestSpecification =
            buildTestSpecification(appApk, testApk)
                    .setAutoGoogleLogin(autoGoogleLogin)
                    .setDisablePerformanceMetrics(disablePerformanceMetrics)
                    .setDisableVideoRecording(disableVideoRecording)
                    .setTestTimeout(testTimeout)
                    .setTestSetup(TestSetup()
                            .setDirectoriesToPull(directoriesToPull.toList())
                            .setEnvironmentVariables(environmentVariables.map { (key, value) ->
                                EnvironmentVariable().setKey(key).setValue(value)
                            })
                            .setNetworkProfile(networkProfile))

    internal abstract fun buildTestSpecification(appApk: FileReference, testApk: FileReference): TestSpecification
}

class InstrumentationTestConfig : TestConfig(TestType.INSTRUMENTATION) {
    var testRunnerClass: String? = null
    var useOrchestrator: Boolean? = null
    val testTargets = TestTargetsBuilder()

    fun targets(configureClosure: Closure<*>) {
        ConfigureUtil.configure(configureClosure, testTargets)
    }

    fun targets(configure: TestTargetsBuilder.() -> Unit) {
        testTargets.configure()
    }

    override fun buildTestSpecification(appApk: FileReference, testApk: FileReference): TestSpecification =
            TestSpecification().setAndroidInstrumentationTest(
                    AndroidInstrumentationTest()
                            .setAppApk(appApk)
                            .setTestApk(testApk)
                            .setTestRunnerClass(testRunnerClass)
                            .setTestTargets(testTargets.build())
                            .setOrchestratorOption(OrchestratorOption.map(useOrchestrator)))
}

enum class OrchestratorOption {
    ORCHESTRATOR_OPTION_UNSPECIFIED,
    USE_ORCHESTRATOR,
    DO_NOT_USE_ORCHESTRATOR;

    companion object {
        fun map(option: Boolean?): String =
                when (option) {
                    null -> ORCHESTRATOR_OPTION_UNSPECIFIED
                    true -> USE_ORCHESTRATOR
                    false -> DO_NOT_USE_ORCHESTRATOR
                }.name
    }
}

class TestTargetsBuilder {
    val targets = mutableListOf<String>()

    fun addPackage(packageName: String) = targets.add("package $packageName")
    fun addClass(className: String) = targets.add("class $className")
    fun addMethod(className: String, methodName: String) = targets.add("class $className#$methodName")

    internal fun build(): List<String> = targets.toList()
}

class RoboTestConfig : TestConfig(TestType.ROBO) {
    var appInitialActivity: String? = null
    var maxDepth: Int? = null
    var maxSteps: Int? = null

    private val roboDirectives = RoboDirectivesBuilder()

    fun roboDirectives(configureClosure: Closure<*>) =
            ConfigureUtil.configure(configureClosure, roboDirectives)

    fun roboDirectives(configure: RoboDirectivesBuilder.() -> Unit) =
            roboDirectives.configure()

    override fun buildTestSpecification(appApk: FileReference, testApk: FileReference): TestSpecification =
            TestSpecification().setAndroidRoboTest(
                    AndroidRoboTest()
                            .setAppApk(appApk)
                            .setAppInitialActivity(appInitialActivity)
                            .setMaxDepth(maxDepth)
                            .setMaxSteps(maxSteps)
                            .setRoboDirectives(roboDirectives.build()))
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

package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidInstrumentationTest
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.TestTargets
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil
import java.io.Serializable
import javax.inject.Inject

internal open class DefaultInstrumentationTest @Inject constructor(name: String = "instrumentation")
    : AbstractTestConfig(name, TestType.INSTRUMENTATION), InstrumentationTest, Serializable {

    companion object {
        @JvmStatic val serialVersionUID: Long = 1L
    }

    override var testRunnerClass: String? = null
    override var useOrchestrator: Boolean? = null
    override val testTargets: TestTargets = DefaultTestTargets()

    override val requiresTestApk: Boolean = true

    override fun targets(configure: Closure<*>): TestTargets =
        testTargets.apply { ConfigureUtil.configure(configure, this) }

    override fun targets(configure: TestTargets.() -> Unit): TestTargets =
        testTargets.apply(configure)

    override fun buildTestSpecification(appApk: FileReference, testApk: FileReference?): TestSpecification =
            TestSpecification().setAndroidInstrumentationTest(
                    AndroidInstrumentationTest()
                            .setAppApk(appApk)
                            .setTestApk(testApk)
                            .setTestRunnerClass(testRunnerClass)
                            .setTestTargets(testTargets.targets.toList())
                            .setOrchestratorOption(useOrchestrator.toOrchestratorOption()))

    private fun Boolean?.toOrchestratorOption(): String = when (this) {
        null -> "ORCHESTRATOR_OPTION_UNSPECIFIED"
        false -> "DO_NOT_USE_ORCHESTRATOR"
        true -> "USE_ORCHESTRATOR"
    }
}

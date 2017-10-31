package com.simple.gradle.testlab.tasks

import com.google.testing.model.AndroidInstrumentationTest
import com.google.testing.model.TestSpecification
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil
import com.simple.gradle.testlab.model.TestTargetsBuilder

class InstrumentationTest : AbstractTestLabTask() {
    var testPackageId: String? = null
    var testRunnerClass: String? = null
    var useOrchestrator: Boolean = false
    val testTargets = TestTargetsBuilder()

    fun targets(configureClosure: Closure<*>): InstrumentationTest {
        ConfigureUtil.configure(configureClosure, testTargets)
        return this
    }

    fun targets(configure: TestTargetsBuilder.() -> Unit): InstrumentationTest {
        testTargets.configure()
        return this
    }

    override fun buildTestSpecification(): TestSpecification = TestSpecification()
            .setAndroidInstrumentationTest(AndroidInstrumentationTest())

    private fun build(): AndroidInstrumentationTest = AndroidInstrumentationTest()
            .setTestPackageId(testPackageId)
            .setTestRunnerClass(testRunnerClass)
            .setTestTargets(testTargets.build())
            .setOrchestratorOption(if (useOrchestrator) "true" else null)
}
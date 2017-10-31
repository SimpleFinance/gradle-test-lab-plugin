package com.simple.gradle.testlab.model

import com.google.testing.model.AndroidInstrumentationTest
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil

class InstrumentationTestBuilder {
    var testRunnerClass: String? = null
    var useOrchestrator: Boolean = false
    val testTargets = TestTargetsBuilder()

    fun targets(configureClosure: Closure<*>) {
        ConfigureUtil.configure(configureClosure, testTargets)
    }

    fun targets(configure: TestTargetsBuilder.() -> Unit) {
        testTargets.configure()
    }

    internal fun build(): AndroidInstrumentationTest = AndroidInstrumentationTest()
            .setTestRunnerClass(testRunnerClass)
            .setTestTargets(testTargets.build())
            .setOrchestratorOption(if (useOrchestrator) "true" else null)
}

class TestTargetsBuilder {
    val targets = mutableListOf<String>()

    fun addPackage(packageName: String) = targets.add("package $packageName")
    fun addClass(className: String) = targets.add("class $className")
    fun addMethod(className: String, methodName: String) = targets.add("class $className#$methodName")

    internal fun build(): List<String> = targets.toList()
}

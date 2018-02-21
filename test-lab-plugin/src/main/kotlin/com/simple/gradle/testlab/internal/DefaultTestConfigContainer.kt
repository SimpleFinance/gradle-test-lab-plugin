package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestConfigContainer
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer
import org.gradle.internal.reflect.Instantiator

open class DefaultTestConfigContainer(instantiator: Instantiator) :
    DefaultPolymorphicDomainObjectContainer<TestConfig>(TestConfig::class.java, instantiator),
    TestConfigContainer {

    override fun instrumentation(configure: Action<in InstrumentationTest>): InstrumentationTest =
        create("instrumentation", InstrumentationTest::class.java, configure)

    override fun robo(configure: Action<in RoboTest>): RoboTest =
        create("robo", RoboTest::class.java, configure)

    private fun <T : TestConfig> addTestConfig(testConfig: T, configure: Action<in T>): T {
        configure.execute(testConfig)
        add(testConfig)
        return testConfig
    }

    override fun handleAttemptToAddItemWithNonUniqueName(o: TestConfig) {
        throw InvalidUserDataException("Test config with name '${o.name}' added multiple times")
    }
}

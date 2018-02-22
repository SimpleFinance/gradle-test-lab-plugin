package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestConfigContainer
import groovy.lang.Closure
import org.gradle.api.InvalidUserDataException
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ConfigureUtil

open class DefaultTestConfigContainer(instantiator: Instantiator) :
    DefaultPolymorphicDomainObjectContainer<TestConfig>(TestConfig::class.java, instantiator),
    TestConfigContainer {

    override fun instrumentation(configure: Closure<*>): InstrumentationTest =
        DefaultInstrumentationTest().apply {
            ConfigureUtil.configure(configure, this)
            add(this)
        }

    override fun instrumentation(name: String, configure: InstrumentationTest.() -> Unit): InstrumentationTest =
        DefaultInstrumentationTest(name).apply {
            configure()
            add(this)
        }

    override fun robo(configure: Closure<*>): RoboTest =
        DefaultRoboTest().apply {
            ConfigureUtil.configure(configure, this)
            add(this)
        }

    override fun robo(name: String, configure: RoboTest.() -> Unit): RoboTest =
        DefaultRoboTest(name).apply {
            configure()
            add(this)
        }

    override fun handleAttemptToAddItemWithNonUniqueName(o: TestConfig) {
        throw InvalidUserDataException("Test config with name '${o.name}' added multiple times")
    }
}

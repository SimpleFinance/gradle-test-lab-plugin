package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer

/**
 * Container of test configurations. For each configuration and application variant,
 * a test task will be created which executes the configuration.
 *
 * Add an instrumentation test with [instrumentation]. Add a Robo test with [robo].
 */
interface TestConfigContainer : ExtensiblePolymorphicDomainObjectContainer<TestConfig> {
    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
    fun instrumentation(configure: Closure<*>): InstrumentationTest

    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
    fun instrumentation(name: String = "instrumentation", configure: InstrumentationTest.() -> Unit): InstrumentationTest

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(configure: Closure<*>): RoboTest

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(name: String = "robo", configure: RoboTest.() -> Unit): RoboTest
}

package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer

interface TestConfigContainer : ExtensiblePolymorphicDomainObjectContainer<TestConfig> {
    fun instrumentation(configure: Closure<*>): InstrumentationTest
    fun instrumentation(name: String = "instrumentation", configure: InstrumentationTest.() -> Unit): InstrumentationTest

    fun robo(configure: Closure<*>): RoboTest
    fun robo(name: String = "robo", configure: RoboTest.() -> Unit): RoboTest
}

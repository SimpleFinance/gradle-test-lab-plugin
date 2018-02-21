package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer

interface TestConfigContainer : ExtensiblePolymorphicDomainObjectContainer<TestConfig> {
    fun instrumentation(configure: Action<in InstrumentationTest>): InstrumentationTest

    fun robo(configure: Action<in RoboTest>): RoboTest
}

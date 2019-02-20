package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider

interface TestConfigContainer : ExtensiblePolymorphicDomainObjectContainer<TestConfig> {

    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
    fun instrumentation(
        name: String,
        configure: Action<in InstrumentationTest>
    ): NamedDomainObjectProvider<InstrumentationTest>

    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
//    fun instrumentation(
//        name: String,
//        configure: Closure<InstrumentationTest>
//    ): NamedDomainObjectProvider<InstrumentationTest>

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(name: String, configure: Action<in RoboTest>): NamedDomainObjectProvider<RoboTest>
//
//    /** Configure and add a [Robo test][RoboTest] to this container. */
//    fun robo(name: String, configure: Closure<RoboTest>): NamedDomainObjectProvider<RoboTest>
}
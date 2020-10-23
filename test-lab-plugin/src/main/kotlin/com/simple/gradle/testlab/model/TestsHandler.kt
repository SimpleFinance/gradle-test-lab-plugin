package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.kotlin.dsl.GradleDsl

@GradleDsl
interface TestsHandler {

    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
    fun instrumentation(
        name: String,
        configure: Action<InstrumentationTest>
    ): NamedDomainObjectProvider<InstrumentationTest>

    fun instrumentation(
        name: String,
        configure: Closure<InstrumentationTest>
    ): NamedDomainObjectProvider<InstrumentationTest>

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(name: String, configure: Action<RoboTest>): NamedDomainObjectProvider<RoboTest>

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(name: String, configure: Closure<RoboTest>): NamedDomainObjectProvider<RoboTest>
}

package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestsHandler
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.kotlin.dsl.register
import org.gradle.util.ConfigureUtil

internal class DefaultTestsHandler(
    private val tests: PolymorphicDomainObjectContainer<TestConfig>
) : TestsHandler {
    override fun instrumentation(
        name: String,
        configure: Action<InstrumentationTest>
    ): NamedDomainObjectProvider<InstrumentationTest> = tests.register(name, InstrumentationTest::class, configure)

    override fun instrumentation(
        name: String,
        configure: Closure<InstrumentationTest>
    ): NamedDomainObjectProvider<InstrumentationTest> = instrumentation(name, ConfigureUtil.configureUsing(configure))

    override fun robo(name: String, configure: Action<RoboTest>): NamedDomainObjectProvider<RoboTest> =
        tests.register(name, RoboTest::class, configure)

    override fun robo(name: String, configure: Closure<RoboTest>): NamedDomainObjectProvider<RoboTest> =
        robo(name, ConfigureUtil.configureUsing(configure))
}

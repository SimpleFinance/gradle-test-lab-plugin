package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestConfigContainer
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer
import org.gradle.internal.reflect.Instantiator
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

internal open class DefaultTestConfigContainer @Inject constructor(
    instantiator: Instantiator,
    callbackDecorator: CollectionCallbackActionDecorator
) : DefaultPolymorphicDomainObjectContainer<TestConfig>(TestConfig::class.java, instantiator, callbackDecorator),
    TestConfigContainer {

    override fun instrumentation(
        name: String,
        configure: Action<in InstrumentationTest>
    ): NamedDomainObjectProvider<InstrumentationTest> =
        register(name, InstrumentationTest::class, configure)

    override fun robo(name: String, configure: Action<in RoboTest>): NamedDomainObjectProvider<RoboTest> =
        register(name, RoboTest::class, configure)
}

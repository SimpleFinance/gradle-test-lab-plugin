package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestConfigHandler
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject
import kotlin.reflect.KClass

@Suppress("UnstableApiUsage")
internal open class DefaultTestConfigHandler @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val testConfigs: MapProperty<String, in TestConfig>
) : TestConfigHandler {

    private fun <T : TestConfig, S : T> register(
        name: String,
        type: KClass<S>,
        configure: Action<T>
    ): Provider<T> {
        check(!testConfigs.getting(name).isPresent) {
            "Firebase test config '$name' is already registered."
        }
        return providers.provider<T> {
            objects.newInstance(type, name).apply(configure::execute)
        }.also { testConfigs.put(name, it) }
    }

    override fun instrumentation(
        name: String,
        configure: Action<InstrumentationTest>
    ): Provider<InstrumentationTest> =
        register(name, DefaultInstrumentationTest::class, configure)

    override fun robo(name: String, configure: Action<RoboTest>): Provider<RoboTest> =
        register(name, DefaultRoboTest::class, configure)
}

package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.provider.Provider

/**
 * Container of test configurations. For each configuration and application variant,
 * a test task will be created which executes the configuration.
 *
 * Add an instrumentation test with [instrumentation]. Add a Robo test with [robo].
 */
@Suppress("UnstableApiUsage")
interface TestConfigHandler {
    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
    fun instrumentation(
        configure: Action<InstrumentationTest>
    ): Provider<InstrumentationTest> = instrumentation("instrumentation", configure)

    /** Configure and add an [instrumentation test][InstrumentationTest] to this container. */
    fun instrumentation(
        name: String,
        configure: Action<InstrumentationTest>
    ): Provider<InstrumentationTest>

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(configure: Action<RoboTest>): Provider<RoboTest> = robo("robo", configure)

    /** Configure and add a [Robo test][RoboTest] to this container. */
    fun robo(name: String, configure: Action<RoboTest>): Provider<RoboTest>
}

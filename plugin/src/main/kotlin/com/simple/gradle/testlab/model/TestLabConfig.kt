package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

open class TestLabConfig(private val project: Project) {
    @Input val googleApi: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @Input val testConfig: Property<TestConfig> = project.objects.property(TestConfig::class.java)
    @Input val devices: ListProperty<Device> = project.objects.listProperty(Device::class.java)

    fun googleApi(configure: Closure<*>): GoogleApiConfig = GoogleApiConfig().apply {
        project.configure(this, configure)
        googleApi.set(this)
    }

    fun googleApi(configure: GoogleApiConfig.() -> Unit): GoogleApiConfig = GoogleApiConfig().apply {
        configure()
        googleApi.set(this)
    }

    fun setTestConfig(config: TestConfig) = testConfig.set(config)

    fun instrumentation(configure: Closure<*>): InstrumentationTestConfig =
            InstrumentationTestConfig().apply { project.configure(this, configure) }

    fun instrumentation(configure: InstrumentationTestConfig.() -> Unit): InstrumentationTestConfig =
            InstrumentationTestConfig().apply { configure() }

    fun robo(configure: Closure<*>): RoboTestConfig =
            RoboTestConfig().apply { project.configure(this, configure) }

    fun robo(configure: RoboTestConfig.() -> Unit): RoboTestConfig =
            RoboTestConfig().apply { configure() }

    fun device(configure: Closure<*>): Device = Device().apply {
        project.configure(this, configure)
        devices.set(devices.get().plus(this))
    }

    fun device(configure: Device.() -> Unit): Device = Device().apply {
        configure()
        devices.set(devices.get().plus(this))
    }
}

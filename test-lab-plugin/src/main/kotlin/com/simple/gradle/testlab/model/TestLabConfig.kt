package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

open class TestLabConfig(private val project: Project) {
    @Input @Optional val appPackageId: Property<String> = project.objects.property(String::class.java)
    @Input @Optional val testPackageId: Property<String> = project.objects.property(String::class.java)
    @Input val googleApi: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @Input val testConfig: Property<TestConfig> = project.objects.property(TestConfig::class.java)
    @Input val devices: ListProperty<Device> = project.objects.listProperty(Device::class.java)
    @Input val artifacts: Property<Artifacts> = project.objects.property(Artifacts::class.java)

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

    fun artifacts(configure: Closure<*>): Artifacts = Artifacts().apply {
        project.configure(this, configure)
        artifacts.set(this)
    }

    fun artifacts(configure: Artifacts.() -> Unit): Artifacts = Artifacts().apply {
        configure()
        artifacts.set(this)
    }
}

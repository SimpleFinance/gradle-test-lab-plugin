package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File

open class TestLabConfig(private val project: Project) {
    @Input val appApk = project.objects.property(File::class.java)
    @Input val testApk = project.objects.property(File::class.java)
    @Input val googleApi: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @Input val instrumentation: Property<InstrumentationTestBuilder> = project.objects.property(InstrumentationTestBuilder::class.java)
    @Input val robo: Property<RoboTestBuilder> = project.objects.property(RoboTestBuilder::class.java)
    @Input val matrix: Property<Matrix> = project.objects.property(Matrix::class.java)

    fun googleApi(closure: Closure<*>): TestLabConfig {
        val config = GoogleApiConfig()
        project.configure(config, closure)
        googleApi.set(config)
        return this
    }

    fun googleApi(action: Action<GoogleApiConfig>): TestLabConfig {
        val config = GoogleApiConfig()
        action.execute(config)
        googleApi.set(config)
        return this
    }

    fun instrumentation(closure: Closure<*>): TestLabConfig {
        val builder = InstrumentationTestBuilder()
        project.configure(builder, closure)
        instrumentation.set(builder)
        return this
    }

    fun instrumentation(action: Action<InstrumentationTestBuilder>): TestLabConfig {
        val builder = InstrumentationTestBuilder()
        action.execute(builder)
        instrumentation.set(builder)
        return this
    }

    fun matrix(closure: Closure<*>): TestLabConfig {
        val config = Matrix()
        project.configure(config, closure)
        matrix.set(config)
        return this
    }

    fun matrix(action: Action<Matrix>): TestLabConfig {
        val config = Matrix()
        action.execute(config)
        matrix.set(config)
        return this
    }
}

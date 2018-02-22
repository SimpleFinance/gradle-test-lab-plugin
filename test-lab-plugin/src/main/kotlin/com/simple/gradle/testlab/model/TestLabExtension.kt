package com.simple.gradle.testlab.model

import groovy.lang.Closure
import org.gradle.internal.HasInternalProtocol

@HasInternalProtocol
interface TestLabExtension {
    val googleApi: GoogleApi
    val tests: TestConfigContainer

    fun googleApi(configure: Closure<*>): GoogleApi
    fun googleApi(configure: GoogleApi.() -> Unit): GoogleApi

    fun tests(configure: Closure<*>): TestConfigContainer
    fun tests(configure: TestConfigContainer.() -> Unit): TestConfigContainer
}

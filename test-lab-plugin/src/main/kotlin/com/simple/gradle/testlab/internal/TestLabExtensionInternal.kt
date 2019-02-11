package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.TestLabExtension
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
internal interface TestLabExtensionInternal : TestLabExtension {
    val prefix: String
    val testsInternal: Provider<List<TestConfigInternal>>
}

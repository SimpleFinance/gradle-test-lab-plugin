package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.TestLabExtension

@Suppress("UnstableApiUsage")
internal interface TestLabExtensionInternal : TestLabExtension {
    val prefix: String
}

package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.TestLabExtension

internal interface TestLabExtensionInternal : TestLabExtension {
    val prefix: String
}

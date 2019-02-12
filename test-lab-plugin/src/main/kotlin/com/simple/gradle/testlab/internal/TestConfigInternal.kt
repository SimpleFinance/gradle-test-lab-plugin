package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.Artifact
import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.TestConfig
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
internal interface TestConfigInternal : TestConfig {
    val artifacts: MutableSet<Artifact>
    val devices: ListProperty<Device>
    val requiresTestApk: Boolean
    val testType: TestType

    fun testSpecification(
        appApk: FileReference,
        testApk: FileReference?,
        additionalApks: List<FileReference>
    ): Provider<TestSpecification>
}

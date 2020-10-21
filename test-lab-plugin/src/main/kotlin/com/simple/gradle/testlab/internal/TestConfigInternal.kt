package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.internal.artifacts.Artifact
import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.TestConfig
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested

@Suppress("UnstableApiUsage")
internal interface TestConfigInternal : TestConfig {
    @get:Input val artifacts: SetProperty<Artifact>
    @get:Nested val devices: ListProperty<Device>
    @get:Nested val files: ListProperty<DeviceFile>
    @get:Internal val requiresTestApk: Boolean
    @get:Internal val testType: TestType

    fun testSpecification(files: List<AppFile>): TestSpecification
}

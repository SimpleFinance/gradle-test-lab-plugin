package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.internal.artifacts.ArtifactsInternal
import com.simple.gradle.testlab.model.TestConfig

internal interface TestConfigInternal : TestConfig {
    override val artifacts: ArtifactsInternal
    val requiresTestApk: Boolean
    val testType: TestType

    fun testSpecification(appApk: FileReference, testApk: FileReference?): TestSpecification
}

package com.simple.gradle.testlab.tasks

import com.google.testing.model.TestSpecification
import org.gradle.api.DefaultTask

abstract class AbstractTestLabTask : DefaultTask() {
    protected abstract fun buildTestSpecification(): TestSpecification
}
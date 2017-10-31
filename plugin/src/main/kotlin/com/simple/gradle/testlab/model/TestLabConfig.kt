package com.simple.gradle.testlab.model

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import java.io.File

open class TestLabConfig(project: Project) {
    @Input val appApk = project.objects.property(File::class.java)
    @Input val testApk = project.objects.property(File::class.java)
    @Input val googleApi = project.objects.property(GoogleApiConfig::class.java)
    @Input val instrumentation = project.objects.property(InstrumentationTestBuilder::class.java)
    @Input val robo = project.objects.property(RoboTestBuilder::class.java)
    @Input val matrix = project.objects.property(Matrix::class.java)
}

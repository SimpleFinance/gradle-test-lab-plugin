package com.simple.gradle.testlab

import com.simple.gradle.testlab.model.TestLabConfig
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestLabPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("testLab", TestLabExtension(project))
    }
}

open class TestLabExtension(project: Project) : TestLabConfig(project)
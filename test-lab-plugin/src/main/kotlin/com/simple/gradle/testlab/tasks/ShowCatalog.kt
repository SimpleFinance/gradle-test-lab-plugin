package com.simple.gradle.testlab.tasks

import com.simple.gradle.testlab.internal.CatalogEnvironment
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.testEnvironmentCatalog
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.HelpTasksPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class ShowCatalog @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {
    val googleApi = objects.property<GoogleApiConfig>()

    init {
        group = HelpTasksPlugin.HELP_GROUP
        description = "Show Test Lab test environment catalog"
    }

    private val api by lazy {
        GoogleApi(googleApi.get(), logger)
    }

    @TaskAction
    fun showCatalog() {
        val catalog = api.testing.testEnvironmentCatalog(CatalogEnvironment.ANDROID)
        logger.lifecycle(catalog.toPrettyString())
    }
}

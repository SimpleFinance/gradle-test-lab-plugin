package com.simple.gradle.testlab.tasks

import com.simple.gradle.testlab.internal.GoogleApiInternal
import com.simple.gradle.testlab.internal.catalog.CatalogEnvironment
import com.simple.gradle.testlab.internal.catalog.testEnvironmentCatalog
import com.simple.gradle.testlab.model.GoogleApi
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class ShowCatalog @Inject constructor(
    objects: ObjectFactory
) : DefaultTask()  {
    val googleApi = objects.property<GoogleApi>()

    private val api by lazy {
        GoogleApiInternal(googleApi.get())
    }

    @TaskAction
    fun showCatalog() {
        val catalog = api.testing.testEnvironmentCatalog(CatalogEnvironment.ANDROID)
        logger.lifecycle(catalog.toPrettyString())
    }
}
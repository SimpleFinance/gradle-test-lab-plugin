package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApi
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.Serializable
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultGoogleApi @Inject constructor(
    objects: ObjectFactory
) : GoogleApi, Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    override val bucketName = objects.property<String>()
    override val serviceCredentials = objects.property<File>()
    override var projectId = objects.property<String>()
}

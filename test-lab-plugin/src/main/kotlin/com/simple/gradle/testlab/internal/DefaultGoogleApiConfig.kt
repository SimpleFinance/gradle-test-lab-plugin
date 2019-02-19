package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApiConfig
import java.io.File
import java.io.Serializable

internal data class DefaultGoogleApiConfig(
    override var bucketName: String? = null,
    override var serviceCredentials: File? = null,
    override var projectId: String? = null
) : GoogleApiConfig, Serializable

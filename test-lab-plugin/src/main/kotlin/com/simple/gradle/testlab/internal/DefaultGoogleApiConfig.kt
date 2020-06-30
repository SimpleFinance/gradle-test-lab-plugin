package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import java.io.File

internal data class DefaultGoogleApiConfig(
    @Input @Optional override var bucketName: String? = null,
    @InputFile @Optional override var serviceCredentials: File? = null,
    @Input @Optional override var projectId: String? = null
) : GoogleApiConfig

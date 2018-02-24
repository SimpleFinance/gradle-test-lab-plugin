package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApi
import java.io.File
import java.io.Serializable

internal class DefaultGoogleApi : GoogleApi, Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    override var bucketName: String? = null
    override var credentialPath: File? = null
    override var projectId: String? = null
}

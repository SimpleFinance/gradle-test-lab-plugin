package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApi
import java.io.File

open class DefaultGoogleApi : GoogleApi {
    override var bucketName: String? = null
    override var credentialPath: File? = null
    override var projectId: String? = null
}

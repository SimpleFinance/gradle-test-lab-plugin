package com.simple.gradle.testlab.model

import java.io.File
import java.io.Serializable

open class GoogleApiConfig : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    var bucketName: String? = null
    var credentialPath: File? = null
    var projectId: String? = null
}
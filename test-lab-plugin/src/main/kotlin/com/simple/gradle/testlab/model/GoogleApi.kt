package com.simple.gradle.testlab.model

import java.io.File

interface GoogleApi {
    var bucketName: String?
    var credentialPath: File?
    var projectId: String?
}

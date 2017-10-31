package com.simple.gradle.testlab.internal

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.storage.StorageScopes
import com.google.api.services.toolresults.ToolResults
import com.google.testing.Testing
import com.google.testing.TestingScopes
import com.simple.gradle.testlab.model.GoogleApiConfig
import java.io.FileInputStream
import java.io.IOException

class GoogleApi(private val config: GoogleApiConfig) {
    companion object {
        private const val APPLICATION_NAME = "gradle-test-lab-plugin"
    }

    private val httpTransport by lazy { GoogleNetHttpTransport.newTrustedTransport() }
    private val jsonFactory by lazy { JacksonFactory.getDefaultInstance() }

    val credential by lazy {
        (config.credentialPath?.let { path -> GoogleCredential.fromStream(FileInputStream(path)) }
                ?: GoogleCredential.getApplicationDefault())
                .createScoped(listOf(TestingScopes.CLOUD_PLATFORM))
    }

    val storage by lazy {
        Storage.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    val testing by lazy {
        Testing.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    val toolResults by lazy {
        ToolResults.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    fun defaultBucketName(): String =
        toolResults.projects()
                .initializeSettings(config.projectId)
                .execute()
                .defaultBucket
}
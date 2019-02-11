package com.simple.gradle.testlab.internal

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.storage.Storage
import com.google.api.services.testing.Testing
import com.google.api.services.testing.TestingScopes
import com.google.api.services.toolresults.ToolResults
import com.simple.gradle.testlab.model.GoogleApi
import java.io.FileInputStream

internal class GoogleApiInternal(private val config: GoogleApi) {
    companion object {
        private const val APPLICATION_NAME = "gradle-test-lab-plugin"
    }

    init {
        check(config.projectId.isPresent) {
            """
                A project ID is required for Firebase tests. You can add it to your build script
                as follows:

                    testLab {
                        googleApi {
                            projectId = "my-project-id"
                        }
                    }
            """.trimIndent()
        }
    }

    private val httpTransport by lazy { GoogleNetHttpTransport.newTrustedTransport() }
    private val jsonFactory by lazy { JacksonFactory.getDefaultInstance() }

    val bucketName by lazy { config.bucketName.orNull ?: defaultBucketName() }

    val projectId by lazy { config.projectId.get() }

    val credential by lazy {
        (config.credentialPath.orNull
            ?.let { GoogleCredential.fromStream(FileInputStream(it)) }
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

    private fun defaultBucketName(): String =
        toolResults.projects()
                .initializeSettings(config.projectId.get())
                .execute()
                .defaultBucket
}

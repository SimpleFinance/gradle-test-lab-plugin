package com.simple.gradle.testlab.internal

import com.google.api.client.googleapis.GoogleUtils
import com.google.api.client.http.HttpBackOffIOExceptionHandler
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler.BackOffRequired
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.HttpUnsuccessfulResponseHandler
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.storage.Storage
import com.google.api.services.testing.Testing
import com.google.api.services.testing.TestingScopes
import com.google.api.services.toolresults.ToolResults
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.GradleException
import java.io.FileInputStream

internal class GoogleApi(val config: GoogleApiConfig) {
    companion object {
        private const val APPLICATION_NAME = "gradle-test-lab-plugin"
    }

    private val httpTransport: HttpTransport by lazy {
        NetHttpTransport.Builder()
                .trustCertificates(GoogleUtils.getCertificateTrustStore())
                .build()
    }

    val jsonFactory: JsonFactory by lazy { JacksonFactory.getDefaultInstance() }

    val bucketName by lazy { config.bucketName ?: defaultBucketName() }

    val projectId by lazy {
        config.projectId
                ?: (credentials as? ServiceAccountCredentials)?.projectId
                ?: throw GradleException("Missing 'projectId' in Test Lab configuration")
    }

    val credentials by lazy {
        (config.serviceCredentials
            ?.let { GoogleCredentials.fromStream(FileInputStream(it)) }
            ?: GoogleCredentials.getApplicationDefault())
            .createScoped(listOf(TestingScopes.CLOUD_PLATFORM))
    }

    val requestInitializer: HttpRequestInitializer by lazy {
        GoogleApiRequestInitializer(credentials)
    }

    val storage by lazy {
        Storage.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    val testing by lazy {
        Testing.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    val toolResults by lazy {
        ToolResults.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }
}

private fun GoogleApi.defaultBucketName(): String =
    toolResults.projects()
        .initializeSettings(config.projectId)
        .execute()
        .defaultBucket

private const val STATUS_TOO_MANY_REQUESTS = 429

private class GoogleApiRequestInitializer(
    credentials: GoogleCredentials
) : HttpRequestInitializer {

    private val credentialsAdapter = HttpCredentialsAdapter(credentials)

    override fun initialize(request: HttpRequest) {
        credentialsAdapter.initialize(request)
        request.ioExceptionHandler = HttpBackOffIOExceptionHandler(ExponentialBackOff())
        request.unsuccessfulResponseHandler = UnsuccessfulResponseHandler(credentialsAdapter)
    }

    private class UnsuccessfulResponseHandler(
        private val credentialsAdapter: HttpCredentialsAdapter
    ) : HttpUnsuccessfulResponseHandler {
        private val backoffHandler = HttpBackOffUnsuccessfulResponseHandler(ExponentialBackOff())
            .setBackOffRequired { response ->
                BackOffRequired.ON_SERVER_ERROR.isRequired(response) ||
                    response.statusCode == STATUS_TOO_MANY_REQUESTS
            }

        override fun handleResponse(
            request: HttpRequest,
            response: HttpResponse,
            supportsRetry: Boolean
        ): Boolean {
            return credentialsAdapter.handleResponse(request, response, supportsRetry) ||
                backoffHandler.handleResponse(request, response, supportsRetry)
        }
    }
}

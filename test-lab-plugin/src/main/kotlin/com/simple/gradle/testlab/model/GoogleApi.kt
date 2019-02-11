package com.simple.gradle.testlab.model

import org.gradle.api.provider.Property
import java.io.File

/** Google API configuration. */
@Suppress("UnstableApiUsage")
interface GoogleApi {
    /** The Google Cloud Storage bucket where the test results will be stored. */
    val bucketName: Property<String>

    /**
     * Path to service account credentials used to execute tests on Firebase and
     * fetch results from Google Cloud Storage. If not provided,
     * [application default credentials](https://cloud.google.com/sdk/gcloud/reference/auth/application-default/)
     * will be used.
     */
    val serviceCredentials: Property<File>

    /**
     * The Firebase/Google Cloud Platform project to use when executing tests and
     * fetching results from Google Cloud Storage.
     */
    val projectId: Property<String>
}

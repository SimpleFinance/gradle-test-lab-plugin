package com.simple.gradle.testlab.model

import java.io.File

/** Google API configuration. */
interface GoogleApiConfig {
    /**
     * The Google Cloud Storage bucket where the test results will be stored.
     *
     * If this is changed from the default, billing must be set up for
     * Google Cloud Storage.
     */
    var bucketName: String?

    /**
     * Path to service account credentials used to execute tests on Firebase and
     * fetch results from Google Cloud Storage. If not provided,
     * [application default credentials](https://cloud.google.com/sdk/gcloud/reference/auth/application-default/)
     * will be used.
     */
    var serviceCredentials: File?

    /**
     * The Firebase/Google Cloud Platform project to use when executing tests and
     * fetching results from Google Cloud Storage.
     */
    var projectId: String?
}

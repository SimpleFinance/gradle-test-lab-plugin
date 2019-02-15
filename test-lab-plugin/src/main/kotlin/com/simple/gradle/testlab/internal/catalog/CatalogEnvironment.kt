package com.simple.gradle.testlab.internal.catalog

import com.google.api.services.testing.Testing
import com.google.api.services.testing.model.TestEnvironmentCatalog

// From https://github.com/google-cloud-sdk/google-cloud-sdk/blob/f587382fd112f238c0d6d5ca3dab8f52d2b5c5f9/lib/googlecloudsdk/third_party/apis/testing/v1/testing_v1_messages.py#L1505-L1510
internal enum class CatalogEnvironment(val displayName: String) {
    ENVIRONMENT_TYPE_UNSPECIFIED("Unspecified"),
    ANDROID("Android"),
    IOS("IOS"),
    NETWORK_CONFIGURATION("Network configurations"),
    PROVIDED_SOFTWARE("Provided software")
}

internal fun Testing.testEnvironmentCatalog(
    environment: CatalogEnvironment
): TestEnvironmentCatalog = testEnvironmentCatalog().get(environment.name).execute()

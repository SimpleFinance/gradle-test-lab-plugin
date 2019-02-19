package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.provider.MapProperty
import org.gradle.internal.HasInternalProtocol

@Suppress("UnstableApiUsage")
@HasInternalProtocol
/**
 * The configuration of Firebase Test Lab tests for this project.
 *
 * Tests require a valid [Google API configuration][googleApi] and a
 * [test configuration][TestConfig].
 */
interface TestLabExtension {
    companion object {
        /** The name of this extension when installed by the [TestLabPlugin] ("testLab"). */
        const val NAME: String = "testLab"
    }

    /**
     * The Google API configuration to use for this project.
     *
     * @see googleApi
     */
    val googleApi: GoogleApiConfig

    /**
     * The container of test configurations for this project.
     *
     * @see tests
     */
    val tests: MapProperty<String, in TestConfig>

    /**
     * Configures the Google API configuration for this project.
     *
     *     plugins {
     *       id("com.simple.gradle.testlab") version "$pluginVersion"
     *     }
     *
     *     testLab {
     *       googleApi {
     *         bucketName = "bucket-name"
     *         serviceCredentials = file("/path/to/credentials.json")
     *         projectId = "example.com:api-project-1234567890"
     *       }
     *     }
     */
    fun googleApi(configure: Action<GoogleApiConfig>)

    /**
     * Configures the test configurations for this project.
     *
     * The tests container defines the possible tests available to run for the project.
     * Two types of tests are currently supported: [instrumentation][InstrumentationTest]
     * and [robo][RoboTest].
     *
     * To add an instrumentation test:
     *
     *     plugins {
     *       id("com.simple.gradle.testlab") version "$pluginVersion"
     *     }
     *
     *     testLab {
     *       tests {
     *         instrumentation("test-name") {
     *           // Configure the test here
     *         }
     *       }
     *     }
     *
     * To add a robo test:
     *
     *     plugins {
     *       id("com.simple.gradle.testlab") version "$pluginVersion"
     *     }
     *
     *     testLab {
     *       tests {
     *         robo("test-name") {
     *           // Configure the test here
     *         }
     *       }
     *     }
     *
     * @see InstrumentationTest
     * @see RoboTest
     * @see TestConfig
     */
    fun tests(configure: Action<TestConfigHandler>)
}

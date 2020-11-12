package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.GradleDsl

/**
 * The configuration of Firebase Test Lab tests for this project.
 *
 * Tests require a valid [Google API configuration][googleApi] and a
 * [test configuration][TestConfig].
 */
@Suppress("UnstableApiUsage")
@GradleDsl
interface TestLabExtension {
    companion object {
        /**
         * The name of this extension when installed by the [TestLabPlugin][com.simple.gradle.testlab.TestLabPlugin]
         * ("testLab").
         */
        const val NAME: String = "testLab"
    }

    /**
     * The Google API configuration to use for this project.
     */
    val googleApi: GoogleApiConfig

    /**
     * Extra values to send with all tests for use with Cloud Functions.
     *
     * For each test, these values will be merged with the test's [clientDetails][TestConfig.clientDetails], with the
     * test's values taking priority for identical keys.
     *
     * These values can be accessed via `testMatrix.clientInfo.details` in a Cloud Functions script. See the
     * [Firebase documentation][https://firebase.google.com/docs/test-lab/extend-with-functions#access_client_details]
     * for more information.
     *
     *     clientDetails.put("pull-request", "https://github.com/owner/repo/pulls/1234")
     */
    val clientDetails: MapProperty<String, String>

    /**
     * The container of test configurations for this project.
     *
     * @see testConfigs
     */
    val testConfigs: PolymorphicDomainObjectContainer<TestConfig>

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
    fun googleApi(configure: Action<in GoogleApiConfig>)

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
    fun tests(configure: Action<in TestsHandler>)
}

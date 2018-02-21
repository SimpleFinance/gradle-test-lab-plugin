package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.Account
import com.google.api.services.testing.model.EnvironmentVariable
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.GoogleAuto
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.Artifacts
import com.simple.gradle.testlab.model.Device
import org.gradle.api.Action

abstract class AbstractTestConfig(
    private var myName: String,
    override val testType: TestType
) : TestConfigInternal {
    override fun getName(): String = myName
    fun setName(name: String) { myName = name }

    override val devices = mutableListOf<DefaultDevice>()
    override var artifacts = DefaultArtifacts()

    override var disablePerformanceMetrics: Boolean = false
    override var disableVideoRecording: Boolean = false
    override var resultsHistoryName: String? = null
    override var testTimeout: String = "900s"

    override var autoGoogleAccount: Boolean = true
    override val directoriesToPull = mutableListOf<String>()
    override val environmentVariables = mutableMapOf<String, String>()
    override var networkProfile: String? = null

    override fun device(configure: Action<in Device>) {
        val device = DefaultDevice()
        configure.execute(device)
        devices.add(device)
    }

    override fun artifacts(configure: Action<in Artifacts>) {
        configure.execute(artifacts)
    }

    override val hasArtifacts: Boolean
        get() = with (artifacts) {
            instrumentation || junit || logcat || video
        }

    override fun testSpecification(appApk: FileReference, testApk: FileReference?): TestSpecification =
            buildTestSpecification(appApk, testApk)
                    .setDisablePerformanceMetrics(disablePerformanceMetrics)
                    .setDisableVideoRecording(disableVideoRecording)
                    .setTestTimeout(testTimeout)
                    .setTestSetup(TestSetup()
                            .setAccount(autoGoogleAccount.toAccount())
                            .setDirectoriesToPull(directoriesToPull.toList())
                            .setEnvironmentVariables(environmentVariables.map { (key, value) ->
                                EnvironmentVariable().setKey(key).setValue(value)
                            })
                            .setNetworkProfile(networkProfile))

    internal abstract fun buildTestSpecification(appApk: FileReference, testApk: FileReference?): TestSpecification

    private fun Boolean?.toAccount(): Account? =
        this?.takeIf { it }?.let { Account().setGoogleAuto(GoogleAuto()) }
}

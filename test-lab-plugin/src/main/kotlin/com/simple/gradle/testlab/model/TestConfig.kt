package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.Named

interface TestConfig : Named {
    val devices: MutableList<out Device>
    val artifacts: Artifacts

    var disablePerformanceMetrics: Boolean
    var disableVideoRecording: Boolean
    var resultsHistoryName: String?
    var testTimeout: String

    var autoGoogleAccount: Boolean
    val directoriesToPull:  MutableList<String>
    val environmentVariables: MutableMap<String, String>
    var networkProfile: String?

    fun device(configure: Action<in Device>)
    fun artifacts(configure: Action<in Artifacts>)
}

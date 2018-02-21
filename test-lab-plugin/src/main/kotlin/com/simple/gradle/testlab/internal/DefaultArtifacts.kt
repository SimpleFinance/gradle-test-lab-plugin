package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.Artifacts

class DefaultArtifacts : Artifacts {
    override var instrumentation: Boolean = false
    override var junit: Boolean = false
    override var logcat: Boolean = false
    override var video: Boolean = false

    override fun all() {
        instrumentation = true
        junit = true
        logcat = true
        video = true
    }

    override fun instrumentation() { instrumentation = true }
    override fun junit() { junit = true }
    override fun logcat() { logcat = true }
    override fun video() { video = true }
}

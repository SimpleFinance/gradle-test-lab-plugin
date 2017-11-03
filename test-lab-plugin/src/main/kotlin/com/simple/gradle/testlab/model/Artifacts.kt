package com.simple.gradle.testlab.model

class Artifacts {
    internal var instrumentation: Boolean = false
    internal var junit: Boolean = false
    internal var logcat: Boolean = false
    internal var video: Boolean = false

    fun all(): Artifacts = apply {
        instrumentation()
        junit()
        logcat()
        video()
    }
    fun instrumentation(): Artifacts = apply { instrumentation = true }
    fun junit(): Artifacts = apply { junit = true }
    fun logcat(): Artifacts = apply { logcat = true }
    fun video(): Artifacts = apply { video = true }
}

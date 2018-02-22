package com.simple.gradle.testlab.model

interface Artifacts {
    var instrumentation: Boolean
    var junit: Boolean
    var logcat: Boolean
    var video: Boolean

    fun all()
    fun instrumentation()
    fun junit()
    fun logcat()
    fun video()
}

package com.simple.gradle.testlab.model

interface InstrumentationArtifacts : Artifacts {
    var instrumentation: Boolean
    var junit: Boolean
    var logcat: Boolean
    var video: Boolean
}

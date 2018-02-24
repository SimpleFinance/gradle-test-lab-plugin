package com.simple.gradle.testlab.model

interface RoboArtifacts : Artifacts {
    var logcat: Boolean
    var screenshots: Boolean
    var video: Boolean
}

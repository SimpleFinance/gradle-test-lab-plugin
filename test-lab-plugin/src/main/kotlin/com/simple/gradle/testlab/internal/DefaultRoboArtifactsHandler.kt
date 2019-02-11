package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.Artifact
import com.simple.gradle.testlab.model.RoboArtifactsHandler

@Suppress("UnstableApiUsage")
class DefaultRoboArtifactsHandler(
    artifacts: MutableSet<Artifact>
) : RoboArtifactsHandler {
    override fun all() {
        logcat = true
        screenshots = true
        video = true
    }

    override var logcat by artifacts(Artifact.LOGCAT)

    override var screenshots by artifacts(Artifact.SCREENSHOTS)

    override var video by artifacts(Artifact.VIDEO)
}
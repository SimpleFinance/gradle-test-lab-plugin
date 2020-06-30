package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.internal.artifacts.Artifact
import com.simple.gradle.testlab.model.RoboArtifactsHandler
import org.gradle.api.provider.SetProperty

@Suppress("UnstableApiUsage")
internal class DefaultRoboArtifactsHandler(
    artifacts: SetProperty<Artifact>
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

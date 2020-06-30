package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.internal.artifacts.Artifact
import com.simple.gradle.testlab.model.InstrumentationArtifactsHandler
import org.gradle.api.provider.SetProperty

internal class DefaultInstrumentationArtifactsHandler(
    artifacts: SetProperty<Artifact>
) : InstrumentationArtifactsHandler {
    override fun all() {
        instrumentation = true
        junit = true
        logcat = true
        video = true
    }

    override var instrumentation by artifacts(Artifact.INSTRUMENTATION)

    override var junit by artifacts(Artifact.JUNIT)

    override var logcat by artifacts(Artifact.LOGCAT)

    override var video by artifacts(Artifact.VIDEO)
}

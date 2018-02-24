package com.simple.gradle.testlab.internal.artifacts

import com.simple.gradle.testlab.model.Artifact
import com.simple.gradle.testlab.model.Artifact.LOGCAT
import com.simple.gradle.testlab.model.Artifact.SCREENSHOTS
import com.simple.gradle.testlab.model.Artifact.VIDEO
import com.simple.gradle.testlab.model.RoboArtifacts
import org.gradle.api.internal.DefaultDomainObjectSet
import java.io.Serializable

internal class DefaultRoboArtifacts :
    DefaultDomainObjectSet<Artifact>(Artifact::class.java),
    RoboArtifacts,
    ArtifactsInternal,
    Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L
        private val ALL = arrayOf(LOGCAT, SCREENSHOTS, VIDEO)
    }

    override fun all(): RoboArtifacts = apply { addAll(ALL) }

    override var logcat: Boolean
        get() = contains(LOGCAT)
        set(value) { if (value) add(LOGCAT) else remove(LOGCAT) }

    override var screenshots: Boolean
        get() = contains(SCREENSHOTS)
        set(value) { if (value) add(SCREENSHOTS) else remove(SCREENSHOTS) }

    override var video: Boolean
        get() = contains(VIDEO)
        set(value) { if (value) add(VIDEO) else remove(VIDEO) }
}

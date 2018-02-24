package com.simple.gradle.testlab.internal.artifacts

import com.simple.gradle.testlab.model.Artifact
import com.simple.gradle.testlab.model.Artifact.INSTRUMENTATION
import com.simple.gradle.testlab.model.Artifact.JUNIT
import com.simple.gradle.testlab.model.Artifact.LOGCAT
import com.simple.gradle.testlab.model.Artifact.VIDEO
import com.simple.gradle.testlab.model.InstrumentationArtifacts
import org.gradle.api.internal.DefaultDomainObjectSet
import java.io.Serializable

internal class DefaultInstrumentationArtifacts :
    DefaultDomainObjectSet<Artifact>(Artifact::class.java),
    InstrumentationArtifacts,
    ArtifactsInternal,
    Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L

        private val ALL = arrayOf(INSTRUMENTATION, JUNIT, LOGCAT, VIDEO)
    }

    override fun all(): InstrumentationArtifacts = apply { addAll(ALL) }

    override var instrumentation: Boolean
        get() = contains(INSTRUMENTATION)
        set(value) { if (value) add(INSTRUMENTATION) else remove(INSTRUMENTATION) }

    override var junit: Boolean
        get() = contains(JUNIT)
        set(value) { if (value) add(JUNIT) else remove(JUNIT) }

    override var logcat: Boolean
        get() = contains(LOGCAT)
        set(value) { if (value) add(LOGCAT) else remove(LOGCAT) }

    override var video: Boolean
        get() = contains(VIDEO)
        set(value) { if (value) add(VIDEO) else remove(VIDEO) }
}

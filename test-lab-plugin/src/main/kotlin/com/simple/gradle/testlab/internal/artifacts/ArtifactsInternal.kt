package com.simple.gradle.testlab.internal.artifacts

import com.simple.gradle.testlab.model.Artifact
import com.simple.gradle.testlab.model.Artifacts
import org.gradle.api.DomainObjectSet

interface ArtifactsInternal : Artifacts, DomainObjectSet<Artifact>

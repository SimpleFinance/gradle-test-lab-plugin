package com.simple.gradle.testlab.model

interface Artifacts {
    /** Fetch all available artifacts for this test type. */
    fun all(): Artifacts
}

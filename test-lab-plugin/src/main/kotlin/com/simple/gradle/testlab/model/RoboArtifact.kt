package com.simple.gradle.testlab.model

enum class RoboArtifact(internal vararg val artifacts: Artifact) {
    /** Fetch all available artifacts. */
    ALL(Artifact.LOGCAT, Artifact.SCREENSHOTS, Artifact.VIDEO),

    /** Fetch device logs to `logcat`. */
    LOGCAT(Artifact.LOGCAT),

    /** Fetch captured screenshots to `screenshots/$filename.png` for each screenshot. */
    SCREENSHOTS(Artifact.SCREENSHOTS),

    /** Fetch captured video to `video.mp4`. */
    VIDEO(Artifact.VIDEO)
}

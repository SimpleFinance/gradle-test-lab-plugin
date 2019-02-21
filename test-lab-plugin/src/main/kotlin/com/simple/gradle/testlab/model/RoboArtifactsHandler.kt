package com.simple.gradle.testlab.model

interface RoboArtifactsHandler : ArtifactsHandler {
    /** Fetch device logs to `logcat`. */
    var logcat: Boolean

    /** Fetch captured screenshots to `screenshots/$filename.png` for each screenshot. */
    var screenshots: Boolean

    /** Fetch captured video to `video.mp4`. */
    var video: Boolean
}
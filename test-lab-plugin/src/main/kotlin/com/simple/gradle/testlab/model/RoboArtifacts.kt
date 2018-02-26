package com.simple.gradle.testlab.model

/**
 * Artifacts to fetch after completing a robo test. Results are stored in
 * `${task.outputDir}/$device` for each test device.
 */
interface RoboArtifacts : Artifacts {
    /** Fetch device logs to `logcat`. */
    var logcat: Boolean

    /** Fetch captured screenshots to `screenshots/$filename.png` for each screenshot. */
    var screenshots: Boolean

    /** Fetch captured video to `video.mp4`. */
    var video: Boolean
}

package com.simple.gradle.testlab.internal.artifacts

import com.google.cloud.storage.Storage
import java.io.File

internal class ArtifactFetcherFactory(
    private val storage: Storage,
    private val bucketName: String,
    private val prefix: String,
    private val outputDir: File
) {
    fun createFetcher(suffix: String, artifact: Artifact): ArtifactFetcher {
        val destDir = File(outputDir, suffix).apply { mkdirs() }
        val srcPath = "$prefix/$suffix"

        return when (artifact) {
            Artifact.INSTRUMENTATION ->
                SingleObjectFetcher(storage, bucketName, destDir, srcPath, "instrumentation logs", "instrumentation.results")
            Artifact.JUNIT ->
                JunitFetcher(storage, bucketName, destDir, srcPath)
            Artifact.LOGCAT ->
                SingleObjectFetcher(storage, bucketName, destDir, srcPath, "logcat", "logcat")
            Artifact.SCREENSHOTS ->
                ScreenshotFetcher(storage, bucketName, destDir, srcPath)
            Artifact.VIDEO ->
                SingleObjectFetcher(storage, bucketName, destDir, srcPath, "video", "video.mp4")
        }
    }
}

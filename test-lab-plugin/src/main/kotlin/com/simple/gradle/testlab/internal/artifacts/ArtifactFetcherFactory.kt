package com.simple.gradle.testlab.internal.artifacts

import com.google.api.services.storage.Storage
import com.simple.gradle.testlab.model.Artifact
import java.io.File

internal class ArtifactFetcherFactory(
    private val objects: Storage.Objects,
    private val bucketName: String,
    private val prefix: String,
    private val outputDir: File
) {
    fun createFetcher(suffix: String, artifact: Artifact): ArtifactFetcher {
        val destDir = File(outputDir, suffix).apply { mkdirs() }
        val srcPath = "$prefix/$suffix"

        return when (artifact) {
            Artifact.INSTRUMENTATION ->
                SingleObjectFetcher(objects, bucketName, destDir, srcPath, "instrumentation logs", "instrumentation.results")
            Artifact.JUNIT ->
                JunitFetcher(objects, bucketName, destDir, srcPath)
            Artifact.LOGCAT ->
                SingleObjectFetcher(objects, bucketName, destDir, srcPath, "logcat", "logcat")
            Artifact.SCREENSHOTS ->
                ScreenshotFetcher(objects, bucketName, destDir, srcPath)
            Artifact.VIDEO ->
                SingleObjectFetcher(objects, bucketName, destDir, srcPath, "video", "video.mp4")
        }
    }
}

package com.simple.gradle.testlab.internal

import com.google.api.services.storage.model.StorageObject
import com.google.api.services.testing.model.AndroidDevice
import com.simple.gradle.testlab.model.Artifacts
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

class ArtifactFetcher(
        private val project: Project,
        private val googleApi: GoogleApi,
        private val bucketName: String,
        private val prefix: String,
        private val outputDir: File,
        private val logger: Logger) {

    fun fetch(device: AndroidDevice, artifacts: Artifacts) {
        val suffix = with (device) { "$androidModelId-$androidVersionId-$locale-$orientation" }
        val destDir = File(outputDir, suffix).apply { mkdirs() }

        if (artifacts.instrumentation) fetchInstrumentation(suffix, destDir)
        if (artifacts.junit) fetchJunit(suffix, destDir)
        if (artifacts.logcat) fetchLogcat(suffix, destDir)
        if (artifacts.video) fetchVideo(suffix, destDir)
    }

    private fun fetchInstrumentation(suffix: String, destDir: File) {
        logger.lifecycle("Downloading instrumentation results...")
        fetch("$prefix/$suffix/instrumentation.results", destDir, "instrumentation.results")
    }

    private fun fetchJunit(suffix: String, destDir: File) {
        logger.lifecycle("Downloading JUnit results...")
        val objects = mutableListOf<StorageObject>()
        var results = googleApi.storage.objects().list(bucketName)
                .setPrefix("$prefix/$suffix/test_result_")
                .execute()
        objects.addAll(results.items)
        while (results.nextPageToken != null) {
            results = googleApi.storage.objects().list(bucketName)
                    .setPrefix("$prefix/$suffix/test_result_")
                    .setPageToken(results.nextPageToken)
                    .execute()
            objects.addAll(results.items)
        }
        for (object_ in objects) {
            fetch(object_.name, destDir, object_.name.replaceBeforeLast("/", "").removeRange(0, 1))
        }
    }

    private fun fetchLogcat(suffix: String, destDir: File) {
        logger.lifecycle("Downloading logcat...")
        fetch("$prefix/$suffix/logcat", destDir, "logcat")
    }

    private fun fetchVideo(suffix: String, destDir: File) {
        logger.lifecycle("Downloading video...")
        fetch("$prefix/$suffix/video.mp4", destDir, "video.mp4")
    }

    private fun fetch(object_: String, destDir: File, filename: String) {
        logger.lifecycle("$object_ => ${destDir.toRelativeString(project.projectDir)}/$filename")
        try {
            googleApi.storage.objects().get(bucketName, object_)
                    .executeMediaAndDownloadTo(File(destDir, filename).apply { createNewFile() }.outputStream())
        } catch (e: Exception) {
            logger.warn("Failed to download $object_: ${e.message}")
            logger.debug("Stack trace:", e)
        }
    }
}
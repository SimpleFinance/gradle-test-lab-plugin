package com.simple.gradle.testlab.internal.artifacts

import com.google.api.services.storage.Storage
import com.simple.gradle.testlab.internal.all
import com.simple.gradle.testlab.internal.log
import java.io.File

internal class ScreenshotFetcher(
    objects: Storage.Objects,
    bucketName: String,
    destDir: File,
    private val srcPath: String
): ArtifactFetcher(objects, bucketName, destDir.resolve("screenshots").apply { mkdirs() }) {
    override fun fetch(): List<File> {
        log.info("Fetching screenshots...")
        // TODO call ToolResults.Projects.Histories.Executions.clusters().list()
        return objects.list(bucketName)
            .setPrefix("$srcPath/artifacts/")
            .all()
            .filter { it.name.endsWith(".png") }
            .mapNotNull { doFetch(it.name, it.name.replaceBeforeLast("/", "").removeRange(0, 1)) }
    }
}

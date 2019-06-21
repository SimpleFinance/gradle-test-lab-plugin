package com.simple.gradle.testlab.internal.artifacts

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.prefix
import com.simple.gradle.testlab.internal.log
import java.io.File

internal class ScreenshotFetcher(
    storage: Storage,
    bucketName: String,
    destDir: File,
    private val srcPath: String
) : ArtifactFetcher(storage, bucketName, destDir.resolve("screenshots").apply { mkdirs() }) {
    override fun fetch(): List<File> {
        log.info("Fetching screenshots...")
        // TODO call ToolResults.Projects.Histories.Executions.clusters().list()
        return storage.list(bucketName, prefix("$srcPath/artifacts/"))
            .iterateAll()
            .filter { it.name.endsWith(".png") }
            .mapNotNull { doFetch(it.name, it.name.replaceBeforeLast("/", "").removeRange(0, 1)) }
    }
}

package com.simple.gradle.testlab.internal.artifacts

import com.google.cloud.storage.Storage
import com.simple.gradle.testlab.internal.log
import java.io.File

internal class SingleObjectFetcher(
    storage: Storage,
    bucketName: String,
    destDir: File,
    private val srcPath: String,
    private val description: String,
    private val filename: String
) : ArtifactFetcher(storage, bucketName, destDir) {
    override fun fetch(): List<File> {
        log.info("Fetching $description...")
        return listOfNotNull(doFetch("$srcPath/$filename", filename))
    }
}

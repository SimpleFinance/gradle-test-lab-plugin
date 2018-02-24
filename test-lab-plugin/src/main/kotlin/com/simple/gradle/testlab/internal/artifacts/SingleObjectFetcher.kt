package com.simple.gradle.testlab.internal.artifacts

import com.google.api.services.storage.Storage
import com.simple.gradle.testlab.internal.log
import java.io.File

internal class SingleObjectFetcher(
    objects: Storage.Objects,
    bucketName: String,
    destDir: File,
    private val srcPath: String,
    private val description: String,
    private val filename: String
) : ArtifactFetcher(objects, bucketName, destDir) {
    override fun fetch(): List<File> {
        log.info("Fetching $description...")
        return listOfNotNull(doFetch("$srcPath/$filename", filename))
    }
}

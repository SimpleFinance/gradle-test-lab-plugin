package com.simple.gradle.testlab.internal.artifacts

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.simple.gradle.testlab.internal.log
import java.io.File

internal abstract class ArtifactFetcher(
    val storage: Storage,
    val bucketName: String,
    private val destDir: File
) {
    abstract fun fetch(): List<File>

    protected fun doFetch(objectPath: String, filename: String): File? =
        try {
            log.info("$objectPath => $destDir")
            val dst = File(destDir, filename).apply { createNewFile() }
            storage.get(BlobId.of(bucketName, objectPath))
                .downloadTo(dst.toPath())
            dst
        } catch (e: Exception) {
            log.warn("Failed to download $objectPath: ${e.message}")
            log.debug("Stack trace:", e)
            null
        }
}

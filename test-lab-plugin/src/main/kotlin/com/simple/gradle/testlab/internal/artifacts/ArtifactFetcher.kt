package com.simple.gradle.testlab.internal.artifacts

import com.google.api.services.storage.Storage.Objects
import com.simple.gradle.testlab.internal.log
import java.io.File

internal abstract class ArtifactFetcher(
    val objects: Objects,
    val bucketName: String,
    private val destDir: File
) {
    abstract fun fetch(): List<File>

    protected fun doFetch(objectPath: String, filename: String): File? =
        try {
            log.info("$objectPath => $destDir")
            val dst = File(destDir, filename).apply { createNewFile() }
            objects.get(bucketName, objectPath)
                .executeMediaAndDownloadTo(dst.outputStream())
            dst
        } catch (e: Exception) {
            log.warn("Failed to download $objectPath: ${e.message}")
            log.debug("Stack trace:", e)
            null
        }
}

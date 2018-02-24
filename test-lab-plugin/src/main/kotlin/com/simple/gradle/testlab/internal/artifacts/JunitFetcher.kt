package com.simple.gradle.testlab.internal.artifacts

import com.google.api.services.storage.Storage
import com.simple.gradle.testlab.internal.all
import com.simple.gradle.testlab.internal.log
import java.io.File

internal class JunitFetcher(
    objects: Storage.Objects,
    bucketName: String,
    destDir: File,
    private val srcPath: String
): ArtifactFetcher(objects, bucketName, destDir) {
    override fun fetch(): List<File> {
        log.info("Fetching JUnit results...")
        return objects.list(bucketName)
            .setPrefix("$srcPath/test_result_")
            .all()
            .mapNotNull { doFetch(it.name, it.name.replaceBeforeLast("/", "").removeRange(0, 1)) }
    }
}

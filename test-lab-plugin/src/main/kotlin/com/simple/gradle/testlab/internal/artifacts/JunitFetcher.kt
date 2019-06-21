package com.simple.gradle.testlab.internal.artifacts

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.prefix
import com.simple.gradle.testlab.internal.log
import java.io.File

internal class JunitFetcher(
    storage: Storage,
    bucketName: String,
    destDir: File,
    private val srcPath: String
) : ArtifactFetcher(storage, bucketName, destDir) {
    override fun fetch(): List<File> {
        log.info("Fetching JUnit results...")
        return storage.list(bucketName, prefix("$srcPath/test_result_"))
            .iterateAll()
            .mapNotNull { doFetch(it.name, it.name.replaceBeforeLast("/", "").removeRange(0, 1)) }
    }
}

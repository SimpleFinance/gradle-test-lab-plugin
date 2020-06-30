package com.simple.gradle.testlab.internal.tasks

import com.android.build.api.variant.BuiltArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import com.google.api.services.testing.model.FileReference
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageException
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.IOException

@Suppress("UnstableApiUsage")
abstract class UploadTask(
    layout: ProjectLayout,
    objects: ObjectFactory
) : DefaultTask() {
    private val log = Logging.getLogger(javaClass)

    @Input
    val prefix: Property<String> = objects.property()

    @Internal
    val googleApiConfig: Property<GoogleApiConfig> = objects.property()

    @Internal
    val artifactsLoader: Property<BuiltArtifactsLoader> = objects.property()

    @OutputFile
    val results: RegularFileProperty = objects.fileProperty().apply {
        set(layout.buildDirectory.file("testLab/$name.json"))
    }

    private val api by lazy { GoogleApi(googleApiConfig.get(), logger) }
    private val bucketName by lazy { api.bucketName }

    protected fun findApks(directory: Directory): List<BuiltArtifact>? =
        artifactsLoader.get().load(directory)?.elements?.toList()

    protected fun upload(file: File, suffix: String = ""): FileReference {
        val name = file.name
        val dir = prefix.get()
        log.lifecycle("Uploading $name to $bucketName...")

        try {
            val blob = api.storage.create(
                BlobInfo.newBuilder(BlobId.of(bucketName, "$dir/$suffix$name"))
                    .setContentType("application/octet-stream")
                    .build()
            )
            api.storage.writer(blob).use { writer ->
                var sent = 0L
                val length = file.length()
                while (sent < length) {
                    sent += file.inputStream().channel.transferTo(sent, length - sent, writer)
                }
            }
            log.lifecycle("Uploaded: $name -> ${blob.selfLink}")
        } catch (e: StorageException) {
            throw TaskExecutionException(this, RuntimeException("Failed to upload $name", e))
        } catch (e: IOException) {
            throw TaskExecutionException(this, RuntimeException("Failed to upload $name", e))
        }

        return FileReference().setGcsPath("gs://$bucketName/$dir/$suffix$name")
    }
}

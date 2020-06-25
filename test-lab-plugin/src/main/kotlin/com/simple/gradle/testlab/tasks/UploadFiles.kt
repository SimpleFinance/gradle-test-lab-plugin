package com.simple.gradle.testlab.tasks

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageException
import com.simple.gradle.testlab.internal.DeviceFile
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.UploadedFile
import com.simple.gradle.testlab.internal.log
import com.simple.gradle.testlab.internal.toJson
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.IOException
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class UploadFiles @Inject constructor(
    layout: ProjectLayout,
    objects: ObjectFactory
) : DefaultTask() {
    @InputFile val appApk: RegularFileProperty = objects.fileProperty()
    @InputFile @Optional val testApk: RegularFileProperty = objects.fileProperty()
    @InputFiles val additionalApks: ConfigurableFileCollection = objects.fileCollection()
    @get:Input internal val deviceFiles: ListProperty<DeviceFile> = objects.listProperty()
    @Input val prefix: Property<String> = objects.property()
    @Input val google: Property<GoogleApiConfig> = objects.property()
    @OutputFile val results: RegularFileProperty = objects.fileProperty().apply {
        set(layout.buildDirectory.file("testLab/$name/upload-results.json"))
    }

    private val api by lazy { GoogleApi(google.get(), logger) }
    private val bucketName by lazy { api.bucketName }
    private val dir by prefix

    init {
        description = "Uploads Test Lab artifacts to Google Cloud Storage."
        group = JavaBasePlugin.VERIFICATION_GROUP
    }

    @TaskAction
    fun uploadApk() {
        val paths = UploadResults(
            appApk = upload(appApk.get().asFile),
            testApk = testApk.takeIf { it.isPresent }?.let { upload(it.get().asFile) },
            additionalApks = additionalApks.map { upload(it) },
            deviceFiles = deviceFiles.get().map {
                when (it) {
                    is DeviceFile.Obb ->
                        UploadedFile(DeviceFile.Type.OBB, upload(it.source, "obb/"), it.filename)
                    is DeviceFile.Regular ->
                        UploadedFile(
                            DeviceFile.Type.REGULAR, upload(it.source, "files/"),
                            it.devicePath
                        )
                }
            }
        )
        results.get().asFile.apply { parentFile.mkdirs() }.writeText(paths.toJson())
    }

    private fun upload(file: File, suffix: String = ""): String {
        val name = file.name
        log.lifecycle("Uploading $name to $bucketName...")

        val blob = try {
            api.storage.create(
                BlobInfo.newBuilder(BlobId.of(bucketName, "$dir/$suffix$name"))
                    .setContentType("application/octet-stream")
                    .build(),
                file.readBytes()
            )
        } catch (e: StorageException) {
            throw TaskExecutionException(this, e)
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }

        log.lifecycle("Uploaded: $name -> ${blob.selfLink}")
        return "gs://$bucketName/$dir/$suffix$name"
    }
}

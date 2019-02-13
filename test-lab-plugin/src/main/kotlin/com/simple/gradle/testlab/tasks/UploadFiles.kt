package com.simple.gradle.testlab.tasks

import com.google.api.client.http.InputStreamContent
import com.simple.gradle.testlab.internal.DeviceFile
import com.simple.gradle.testlab.internal.GoogleApiInternal
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.UploadedFile
import com.simple.gradle.testlab.internal.log
import com.simple.gradle.testlab.model.GoogleApi
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
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
    @get:Input val appApk: RegularFileProperty = objects.fileProperty()
    @get:Input val testApk: RegularFileProperty = objects.fileProperty()
    @get:Input val additionalApks: ConfigurableFileCollection = layout.configurableFiles()
    @get:Input internal val deviceFiles: ListProperty<DeviceFile> = objects.listProperty()
    @get:Input val prefix: Property<String> = objects.property()
    @get:Input val google: Property<GoogleApi> = objects.property()
    @get:OutputFile val results: RegularFileProperty = objects.fileProperty().convention(
        layout.buildDirectory.file("testLab/$name/upload-results.json")
    )

    private val api by lazy { GoogleApiInternal(google.get()) }
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
                        UploadedFile(DeviceFile.Type.REGULAR, upload(it.source, "files/"),
                            it.devicePath)
                }
            }
        )
        paths.writeTo(results.get().asFile.apply { parentFile.mkdirs() })
    }

    private fun upload(file: File, suffix: String = ""): String {
        val name = file.name
        log.lifecycle("Uploading $name to $bucketName...")

        val content = InputStreamContent("application/octet-stream", file.inputStream())
        val storageObject = try {
            api.storage.objects().insert(bucketName, null, content)
                .setName("$dir/$suffix$name")
                .execute()
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }

        log.lifecycle("Uploaded: $name -> ${storageObject.selfLink}")
        return "gs://$bucketName/$dir/$suffix$name"
    }
}

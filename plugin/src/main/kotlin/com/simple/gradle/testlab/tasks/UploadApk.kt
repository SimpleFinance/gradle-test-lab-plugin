package com.simple.gradle.testlab.tasks

import com.google.api.client.http.InputStreamContent
import com.google.api.services.storage.model.Bucket
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

open class UploadApk : DefaultTask() {
    @Input val key: Property<String> = project.objects.property(String::class.java)
    @Input val file: Property<File> = project.objects.property(File::class.java)
    @Input val prefix: Property<String> = project.objects.property(String::class.java)
    @Input val google: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @OutputFile var output: File = project.file("${project.buildDir}/test-lab/upload.properties")

    @TaskAction
    fun uploadApk() {
        val apk = file.get()
        val googleConfig = google.get()
        val googleApi = GoogleApi(googleConfig)
        val bucketName = googleConfig.bucketName ?: googleApi.defaultBucketName()

        project.logger.lifecycle("Uploading ${apk.name} to $bucketName...")
        val storageObject = try {
            InputStreamContent("application/octet-stream", FileInputStream(apk))
                    .setLength(apk.length())
                    .let { content -> googleApi.storage.objects().insert(bucketName, null, content) }
                    .setName("${prefix.get()}/${apk.name}")
                    .execute()
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }

        project.logger.lifecycle("Uploaded: ${apk.name} -> ${storageObject.selfLink}")

        output.appendText("${key.get()}=${storageObject.selfLink}")
    }
}

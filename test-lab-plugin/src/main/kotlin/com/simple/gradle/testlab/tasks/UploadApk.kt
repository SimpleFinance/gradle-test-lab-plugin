package com.simple.gradle.testlab.tasks

import com.google.api.client.http.InputStreamContent
import com.google.api.services.testing.model.FileReference
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

open class UploadApk : DefaultTask() {
    @Input val file: Property<File> = project.objects.property(File::class.java)
    @Input val prefix: Property<String> = project.objects.property(String::class.java)
    @Input val google: Property<GoogleApiConfig> = project.objects.property(GoogleApiConfig::class.java)
    @Internal var results: UploadResults? = null

    @TaskAction
    fun uploadApk() {
        val apk = file.get()
        val googleConfig = google.get()
        val googleApi = GoogleApi(googleConfig)
        val bucketName = googleConfig.bucketName ?: googleApi.defaultBucketName()
        val prefix = prefix.get()

        project.logger.lifecycle("Uploading ${apk.name} to $bucketName...")
        val storageObject = try {
            InputStreamContent("application/octet-stream", FileInputStream(apk))
                    .setLength(apk.length())
                    .let { content -> googleApi.storage.objects().insert(bucketName, null, content) }
                    .setName("$prefix/${apk.name}")
                    .execute()
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }

        project.logger.lifecycle("Uploaded: ${apk.name} -> ${storageObject.selfLink}")
        results?.references?.put(apk, FileReference().setGcsPath("gs://$bucketName/$prefix/${apk.name}"))
    }
}

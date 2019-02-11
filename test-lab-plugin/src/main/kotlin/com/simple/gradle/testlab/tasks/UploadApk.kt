package com.simple.gradle.testlab.tasks

import com.google.api.client.http.InputStreamContent
import com.google.api.services.testing.model.FileReference
import com.simple.gradle.testlab.internal.GoogleApiInternal
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.internal.log
import com.simple.gradle.testlab.model.GoogleApi
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class UploadApk @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    @get:Input val file: Property<File> = objects.property()
    @get:Input val prefix: Property<String> = objects.property()
    @get:Input val google: Property<GoogleApi> = objects.property()
    @get:Internal internal var results: UploadResults? = null

    init {
        description = "Upload APK to Firebase."
        group = JavaBasePlugin.VERIFICATION_GROUP
    }

    @TaskAction
    fun uploadApk() {
        val apk = file.get()
        val googleConfig = google.get()
        val googleApi = GoogleApiInternal(googleConfig)
        val bucketName = googleApi.bucketName
        val prefix = prefix.get()

        log.lifecycle("Uploading ${apk.name} to $bucketName...")
        val storageObject = try {
            InputStreamContent("application/octet-stream", FileInputStream(apk))
                    .setLength(apk.length())
                    .let { content -> googleApi.storage.objects().insert(bucketName, null, content) }
                    .setName("$prefix/${apk.name}")
                    .execute()
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }

        log.lifecycle("Uploaded: ${apk.name} -> ${storageObject.selfLink}")
        results?.references?.put(apk, FileReference().setGcsPath("gs://$bucketName/$prefix/${apk.name}"))
    }
}

package com.simple.gradle.testlab.tasks

import com.google.api.client.http.InputStreamContent
import com.simple.gradle.testlab.internal.GoogleApi
import com.simple.gradle.testlab.model.GoogleApiConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

open class UploadApk : DefaultTask() {
    @Input val file = project.objects.property(File::class.java)
    @Input val google = project.objects.property(GoogleApiConfig::class.java)
    @OutputFile val output = project.objects.property(File::class.java)

    @TaskAction
    fun upload() {
        val file = file.get()
        val googleConfig = google.get()
        val googleApi = GoogleApi(googleConfig)

        val bucketName = googleConfig.bucketName ?: try {
            googleApi.defaultBucketName()
        } catch (e: IOException) {
            throw GradleException("Failed to fetch default bucket name", e)
        }

        project.logger.lifecycle("Uploading ${file.name} to $bucketName")
        val storageObject = try {
            InputStreamContent("application/octet-stream", FileInputStream(file))
                    .setLength(file.length())
                    .let { content -> googleApi.storage.objects().insert(bucketName, null, content) }
                    .setName(file.name)
                    .execute()
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }

        output.get().appendText("${file.path} = ${storageObject.selfLink}")
    }
}

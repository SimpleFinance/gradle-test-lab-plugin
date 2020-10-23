package com.simple.gradle.testlab.tasks

import com.simple.gradle.testlab.internal.AppFile
import com.simple.gradle.testlab.internal.tasks.UploadTask
import com.simple.gradle.testlab.internal.toJson
import com.simple.gradle.testlab.model.FileType
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class UploadBundle @Inject constructor(
    layout: ProjectLayout,
    objects: ObjectFactory
) : UploadTask(layout, objects) {

    @InputFile
    val appBundle: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun uploadBundle() {
        val bundle = checkNotNull(appBundle.asFile.get().takeIf { it.exists() }) {
            "App bundle does not exist: ${appBundle.asFile.orNull}"
        }
        val path = upload(bundle)
        val paths = listOf(AppFile(FileType.APP_BUNDLE, path))
        results.get().asFile.apply { parentFile.mkdirs() }.writeText(paths.toJson())
    }
}

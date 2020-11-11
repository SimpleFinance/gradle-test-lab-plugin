package com.simple.gradle.testlab.tasks

import com.simple.gradle.testlab.internal.AppFile
import com.simple.gradle.testlab.internal.DeviceFile
import com.simple.gradle.testlab.internal.tasks.UploadTask
import com.simple.gradle.testlab.internal.toJson
import com.simple.gradle.testlab.model.FileType
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class UploadFiles @Inject constructor(
    layout: ProjectLayout,
    objects: ObjectFactory
) : UploadTask(layout, objects) {

    @InputFiles val additionalApks: ConfigurableFileCollection = objects.fileCollection()
    @Nested val deviceFiles: ListProperty<DeviceFile> = objects.listProperty()
    @InputFile @Optional val roboScript: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun uploadFiles() {
        val apkPaths = additionalApks.map { upload(it) }
        val filePaths = deviceFiles.get().filterIsInstance<DeviceFile.Regular>().map { upload(it.source, "files/") }
        val obbPaths = deviceFiles.get().filterIsInstance<DeviceFile.Obb>().map { upload(it.source, "obb/") }
        val scriptPath = roboScript.map { upload(it.asFile, "robo/") }

        val paths = apkPaths.map { AppFile(FileType.EXTRA_APK, it) } +
            filePaths.map { AppFile(FileType.EXTRA_FILE, it) } +
            obbPaths.map { AppFile(FileType.EXTRA_OBB, it) } +
            listOfNotNull(scriptPath.orNull?.let { AppFile(FileType.ROBO_SCRIPT, it) })

        results.get().asFile.apply { parentFile.mkdirs() }.writeText(paths.toJson())
    }
}

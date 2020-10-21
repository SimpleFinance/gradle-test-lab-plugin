package com.simple.gradle.testlab.tasks

import com.android.build.api.variant.VariantOutputConfiguration.OutputType
import com.simple.gradle.testlab.internal.AppFile
import com.simple.gradle.testlab.internal.tasks.UploadTask
import com.simple.gradle.testlab.internal.toJson
import com.simple.gradle.testlab.model.FileType
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class UploadApk @Inject constructor(
    layout: ProjectLayout,
    objects: ObjectFactory
) : UploadTask(layout, objects) {

    @Input
    val fileType: Property<FileType> = objects.property()

    @InputDirectory
    val apkDirectory: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun uploadApk() {
        val mainType = fileType.get()
        val dir = apkDirectory.get()

        val entries = checkNotNull(findApks(dir)) {
            "No APKs found in directory: $dir"
        }
        val mainEntry = checkNotNull(
            (
                entries.firstOrNull { it.outputType == OutputType.SINGLE }
                    ?: entries.firstOrNull { it.outputType == OutputType.UNIVERSAL }
                )
        ) {
            "Main APK not found in directory: $dir"
        }
        val mainApk = checkNotNull(File(mainEntry.outputFile).takeIf(File::exists)) {
            "Main APK does not exist: ${mainEntry.outputFile}"
        }

        val splitEntries = entries - mainEntry
        val splitApks = splitEntries.map {
            checkNotNull(File(it.outputFile).takeIf(File::exists)) {
                "Split APK does not exist: ${it.outputFile}"
            }
        }

        val mainPath = upload(mainApk)
        val splitPaths = splitApks.map { upload(it) }
        val paths = listOf(AppFile(mainType, mainPath)) + splitPaths.map { AppFile(FileType.EXTRA_APK, it) }

        results.get().asFile.apply { parentFile.mkdirs() }.writeText(paths.toJson())
    }
}

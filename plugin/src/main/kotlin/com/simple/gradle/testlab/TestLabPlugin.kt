package com.simple.gradle.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.model.TestLabConfig
import com.simple.gradle.testlab.tasks.TestLabTask
import com.simple.gradle.testlab.tasks.UploadApk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Random

class TestLabPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            val extension = TestLabExtension(project)
            extensions.add("testLab", extension)

            afterEvaluate {
                plugins.withType<AppPlugin> {
                    extensions.getByType(AppExtension::class.java).testVariants.forEach { variant ->
                        addTestLabTask(project, extension, variant)
                    }
                }
            }
        }
    }

    private fun addTestLabTask(project: Project, extension: TestLabExtension, variant: TestVariant) = project.run {
        val uploadResults = UploadResults()
        val uploadApp = project.addUploadTask(
                "upload${variant.taskName()}AppApk", extension, variant.testedVariant, uploadResults)
        val uploadTest = addUploadTask(
                "upload${variant.taskName()}TestApk", extension, variant, uploadResults)

        tasks {
            "${variant.testedVariant.name}TestLabTest"(TestLabTask::class) {
                dependsOn(uploadApp)
                dependsOn(uploadTest)
                appApk.set(variant.testedVariant.outputs.first().outputFile)
                testApk.set(variant.outputs.first().outputFile)
                google.set(extension.googleApi)
                testConfig.set(extension.testConfig)
                devices.set(extension.devices)
                artifacts.set(extension.artifacts)
                outputDir.set(file("$buildDir/test-results/$name"))
                prefix = extension.prefix
                this.uploadResults = uploadResults
            }
        }
    }

    private fun Project.addUploadTask(
            name: String,
            extension: TestLabExtension,
            variant: BaseVariant,
            uploadResults: UploadResults
    ): Task = tasks.create(name, UploadApk::class.java) {
        dependsOn(variant.assemble)
        file.set(variant.outputs.first().outputFile)
        prefix.set(extension.prefix)
        google.set(extension.googleApi)
        results = uploadResults
    }

    private fun addUploadTasks(
            project: Project,
            extension: TestLabExtension,
            variant: TestVariant,
            uploadResults: UploadResults
    ): List<Task> = project.run {
        mutableListOf<Task>().let { created ->
            val appApk = provider { variant.testedVariant.outputs.first().outputFile }
            val testApk = variant.outputs.first().outputFile

            tasks {
                created.add("upload${variant.taskName()}AppApk"(UploadApk::class) {
                    dependsOn(variant.testedVariant.assemble)
                    file.set(appApk)
                    prefix.set(extension.prefix)
                    google.set(extension.googleApi)
                    results = uploadResults
                })

                created.add("upload${variant.taskName()}TestApk"(UploadApk::class) {
                    dependsOn(variant.assemble)
                    file.set(testApk)
                    prefix.set(extension.prefix)
                    google.set(extension.googleApi)
                    results = uploadResults
                })
            }

            created
        }
    }
}

open class TestLabExtension(project: Project) : TestLabConfig(project) {
    internal val variants = project.objects.listProperty(String::class.java)

    @get:Internal internal val prefix by lazy { getUniquePathPrefix() }

    fun setVariants(names: List<String>) {
        variants.set(names)
    }
}

internal fun BaseVariant.taskName(): String = name.capitalize()
internal fun TestVariant.taskName(): String = testedVariant.taskName()

internal fun getUniquePathPrefix(): String {
    val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val suffixLength = 4
    val randomGenerator = Random()

    val suffix = StringBuilder(suffixLength)
    for (i in 0 until suffixLength) {
        suffix.append(characters[randomGenerator.nextInt(characters.length)])
    }

    val creationTime = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME)
    return "gradle-build_" + creationTime.replace(' ', '_').replace(',', '.') + "_" + suffix
}

package com.simple.gradle.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.simple.gradle.testlab.model.TestLabConfig
import com.simple.gradle.testlab.tasks.UploadApk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.gradle.internal.impldep.com.fasterxml.jackson.databind.util.ISO8601DateFormat
import org.gradle.internal.impldep.org.joda.time.DateTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Random

class TestLabPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            val extension = TestLabExtension(project)
            extensions.add("testLab", extension)

            addAndroidTasks(project, extension)
        }
    }

    private fun addAndroidTasks(project: Project, extension: TestLabExtension) {
        project.afterEvaluate {
            plugins.withType<AppPlugin> {
                val android = extensions.getByType(AppExtension::class.java)
                android.testVariants
                        .forEach { variant ->
                            val uploadTasks = addUploadTasks(project, extension, variant)
                        }
            }
        }
    }

    private fun addUploadTasks(project: Project, extension: TestLabExtension, variant: TestVariant): List<Task> {
        project.run {
            val created = mutableListOf<Task>()
            val appApk = provider { variant.testedVariant.outputs.first().outputFile }
            tasks {
                created.add("upload${variant.taskName()}AppApk"(type = UploadApk::class) {
                    key.set("${variant.name}App")
                    file.set(appApk)
                    prefix.set(extension.prefix)
                    google.set(extension.googleApi)
                })
            }

            if (!extension.instrumentation.isPresent) return created

            val testApk = variant.outputs.first().outputFile
            tasks {
                created.add("upload${variant.taskName()}TestApk"(type = UploadApk::class) {
                    key.set("${variant.name}Test")
                    file.set(testApk)
                    prefix.set(extension.prefix)
                    google.set(extension.googleApi)
                })
            }

            return created
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

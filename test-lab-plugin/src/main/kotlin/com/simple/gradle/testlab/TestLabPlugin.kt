@file:Suppress("UnstableApiUsage")

package com.simple.gradle.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.simple.gradle.testlab.internal.DefaultTestLabExtension
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.TestLabExtensionInternal
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestLabExtension
import com.simple.gradle.testlab.tasks.ShowCatalog
import com.simple.gradle.testlab.tasks.TestLabTest
import com.simple.gradle.testlab.tasks.UploadFiles
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

@Suppress("unused", "UnstableApiUsage")
class TestLabPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val extension = objects.newInstance<DefaultTestLabExtension>()
        extensions.add(TestLabExtension::class.java, TestLabExtension.NAME, extension)

        pluginManager.withPlugin("com.android.application") {
            extension.tests.all {
                val test = this as TestConfigInternal
                the<AppExtension>().applicationVariants.all {
                    addTestLabTasksForApplicationVariant(extension, test, this)
                }
            }
            addCatalogTasks(extension)
        }
    }
}

private fun Project.addTestLabTasksForApplicationVariant(
    extension: TestLabExtensionInternal,
    test: TestConfigInternal,
    variant: ApplicationVariant
) {
    val uploadFiles = tasks.register<UploadFiles>(
        "testLab${taskName(variant, test)}UploadFiles"
    ) {
        dependsOn(variant.packageApplicationProvider)
        appApk.set(layout.file(provider {
            variant.apks().first()
        }))
        additionalApks.from(test.additionalApks)
        deviceFiles.set(test.files)
        prefix.set(extension.prefix)
        google.set(extension.googleApi)
    }

    if (test.requiresTestApk) {
        the<AppExtension>().testVariants.matching { it.testedVariant == variant }.all {
            uploadFiles.configure {
                dependsOn(packageApplicationProvider)
                testApk.set(layout.file(provider { apks().first() }))
            }
        }
    }

    tasks.register<TestLabTest>(
        "testLab${taskName(variant, test)}Test"
    ) {
        description =
            "Runs ${test.testType.name.toLowerCase()} test '${test.name}' " +
                "for the ${variant.name} build on Firebase Test Lab."
        group = JavaBasePlugin.VERIFICATION_GROUP
        dependsOn(uploadFiles)
        appPackageId.set(variant.applicationId)
        googleApiConfig.set(extension.googleApi)
        prefix.set(extension.prefix)
        testConfig.set(test)
        uploadResults.set(uploadFiles.flatMap { it.results })
    }
}

private fun Project.addCatalogTasks(extension: TestLabExtensionInternal) {
    tasks.register<ShowCatalog>("testLabCatalog") {
        googleApi.set(extension.googleApi)
    }
}

private fun taskName(variant: BaseVariant, testConfig: TestConfig): String =
    "${variant.taskName()}${testConfig.taskName()}"

internal fun BaseVariant.taskName(): String = name.capitalize()
internal fun TestConfig.taskName(): String = name.asValidTaskName().capitalize()

internal fun BaseVariant.apks() = outputs.asSequence()
    .filterIsInstance<ApkVariantOutput>()
    .map { it.outputFile }

private fun String.asValidTaskName(): String =
    replace(Regex("[-_.]"), " ")
        .replace(Regex("\\s+(\\S)")) { result: MatchResult ->
            result.groups[1]!!.value.capitalize()
        }
        .let {
            Constants.FORBIDDEN_CHARACTERS.fold(it) { name, c ->
                name.replace(c, Constants.REPLACEMENT_CHARACTER)
            }
        }

private object Constants {
    val FORBIDDEN_CHARACTERS = charArrayOf(' ', '/', '\\', ':', '<', '>', '"', '?', '*', '|')
    const val REPLACEMENT_CHARACTER = '_'
}

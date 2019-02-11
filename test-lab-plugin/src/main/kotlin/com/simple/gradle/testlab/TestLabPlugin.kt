package com.simple.gradle.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.simple.gradle.testlab.internal.DefaultTestLabExtension
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.TestLabExtensionInternal
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestLabExtension
import com.simple.gradle.testlab.tasks.TestLabTest
import com.simple.gradle.testlab.tasks.UploadApk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused", "UnstableApiUsage")
class TestLabPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        pluginManager.withPlugin("com.android.application") {
            val extension = objects.newInstance<DefaultTestLabExtension>()
            extensions.add(TestLabExtension::class.java, TestLabExtension.NAME, extension)

            val uploadResults = UploadResults()

            configure<AppExtension> {
                applicationVariants.all {
                    project.addTestLabTasksForApplicationVariant(extension, this, uploadResults)
                }
                testVariants.all {
                    project.addTestLabTasksForTestVariant(extension, this, uploadResults)
                }
            }
        }
    }
}

private fun Project.addTestLabTasksForApplicationVariant(
    extension: TestLabExtensionInternal,
    variant: ApplicationVariant,
    uploadResults: UploadResults
) {
    variant.onFirstOutput { output ->
        val uploadApp = maybeCreateUploadTask(
            "testLabUpload${variant.taskName()}AppApk", extension, variant, output, uploadResults)

        afterEvaluate {
            extension.testsInternal.get()
                .filterNot { it.requiresTestApk }
                .forEach { testConfig ->
                    createDefaultTestLabTask(extension, testConfig, variant, output, uploadResults)
                        .map { it.dependsOn(uploadApp) }
                }
        }
    }
}

private fun Project.addTestLabTasksForTestVariant(
    extension: TestLabExtensionInternal,
    variant: TestVariant,
    uploadResults: UploadResults
) {
    variant.onFirstOutput { testOutput ->
        val uploadTest = maybeCreateUploadTask(
            "testLabUpload${variant.taskName()}TestApk",
            extension,
            variant,
            testOutput,
            uploadResults
        )

        variant.testedVariant.onFirstOutput { appOutput ->
            val uploadApp = maybeCreateUploadTask(
                "testLabUpload${variant.taskName()}AppApk",
                extension,
                variant.testedVariant,
                appOutput,
                uploadResults
            )

            extension.testsInternal.get()
                .filter { it.requiresTestApk }
                .forEach { testConfig ->
                    createDefaultTestLabTask(
                        extension,
                        testConfig,
                        variant.testedVariant,
                        appOutput,
                        uploadResults
                    ).map {
                        it.dependsOn(uploadApp)
                        it.dependsOn(uploadTest)
                        it.testApk.set(testOutput.outputFile)
                        it.testPackageId.set(variant.applicationId)
                    }
                }
        }
    }
}

private fun Project.maybeCreateUploadTask(
    name: String,
    extension: TestLabExtensionInternal,
    variant: BaseVariant,
    output: BaseVariantOutput,
    uploadResults: UploadResults
): TaskProvider<UploadApk> {
    return if (name in tasks.names) {
        tasks.named<UploadApk>(name)
    } else {
        tasks.register(name, UploadApk::class) {
            val outputType = if (variant is TestVariant) "test" else "app"
            description = "Upload the ${variant.name} $outputType APK to Firebase Test Lab."
            dependsOn(variant.assembleProvider)
            file.set(output.outputFile)
            prefix.set(extension.prefix)
            google.set(extension.googleApi)
            results = uploadResults
        }
    }
}

private fun Project.createDefaultTestLabTask(
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal,
    variant: BaseVariant,
    output: BaseVariantOutput,
    uploadResults: UploadResults
): TaskProvider<TestLabTest> {
    val taskName = "testLab${variant.taskName()}${testConfig.taskName()}Test"
    return tasks.register<TestLabTest>(taskName) {
        description =
            "Runs the ${testConfig.testType.name.toLowerCase()} test '${testConfig.name}' for " +
                "the ${variant.name} build on Firebase Test Lab."
        appApk.set(output.outputFile)
        appPackageId.set(variant.applicationId)
        google.set(extension.googleApi)
        this.testConfig.set(testConfig)
        outputDir.set(file("$buildDir/test-results/$name"))
        prefix = extension.prefix
        this.uploadResults = uploadResults
    }
}

internal fun BaseVariant.taskName(): String = name.capitalize()
internal fun TestVariant.taskName(): String = testedVariant.taskName()
internal fun TestConfig.taskName(): String = name.asValidTaskName().capitalize()

internal fun BaseVariant.onFirstOutput(configure: (BaseVariantOutput) -> Unit) {
    val done = AtomicBoolean()
    outputs.all {
        if (done.getAndSet(true)) return@all
        configure(this)
    }
}

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

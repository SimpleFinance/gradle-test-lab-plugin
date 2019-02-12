package com.simple.gradle.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.simple.gradle.testlab.internal.DefaultTestLabExtension
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.TestLabExtensionInternal
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestLabExtension
import com.simple.gradle.testlab.tasks.TestLabTest
import com.simple.gradle.testlab.tasks.UploadApk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
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

            configure<AppExtension> {
                applicationVariants.all {
                    project.addTestLabTasksForApplicationVariant(extension, this)
                }
            }
        }
    }
}

private fun Project.addTestLabTasksForApplicationVariant(
    extension: TestLabExtensionInternal,
    variant: ApplicationVariant
) {
    afterEvaluate {
        extension.testsInternal.get().forEach { testConfig ->
            createDefaultTestLabTask(extension, testConfig, variant)
        }
    }
}

private fun Project.addUploadTask(
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal,
    appVariant: ApplicationVariant,
    block: (TaskProvider<UploadApk>) -> Unit
) {
    val name = if (testConfig.requiresTestApk) {
        "testLabUpload${appVariant.taskName()}AndTestApks"
    } else {
        "testLabUpload${appVariant.taskName()}Apks"
    }

    return if (name in tasks.names) {
        block(tasks.named<UploadApk>(name))
    } else {
        appVariant.onFirstOutput { appOutput ->
            if (testConfig.requiresTestApk) {
                if (appVariant.testVariant == null) {
                    logger.info(
                        "${testConfig.name}: No test variant for ${appVariant.name}; skipping.")
                    return@onFirstOutput
                }

                appVariant.testVariant.onFirstOutput { testOutput ->
                    maybeRegisterUploadTask(
                        name,
                        extension,
                        testConfig,
                        appOutput,
                        testOutput,
                        block
                    ) {
                        description =
                            "Upload ${appVariant.name} and test APKs to Firebase Test Lab."
                        dependsOn(appVariant.assembleProvider)
                        dependsOn(appVariant.testVariant.assembleProvider)
                    }
                }
            } else {
                maybeRegisterUploadTask(
                    name,
                    extension,
                    testConfig,
                    appOutput,
                    null,
                    block
                ) {
                    dependsOn(appVariant.assembleProvider)
                    description = "Upload ${appVariant.name} APKs to Firebase Test Lab."
                }
            }
        }
    }
}

private fun Project.maybeRegisterUploadTask(
    name: String,
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal,
    appOutput: BaseVariantOutput,
    testOutput: BaseVariantOutput?,
    block: (TaskProvider<UploadApk>) -> Unit,
    configure: UploadApk.() -> Unit
) {
    block(tasks.register(name, UploadApk::class) {
        appApk.set(appOutput.outputFile)
        if (testOutput != null) testApk.set(testOutput.outputFile)
        additionalApks.from(testConfig.additionalApks)
        prefix.set(extension.prefix)
        google.set(extension.googleApi)
        configure()
    })
}

private fun Project.createDefaultTestLabTask(
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal,
    appVariant: ApplicationVariant
) {
    addUploadTask(extension, testConfig, appVariant) { uploadApks ->
        tasks.register<TestLabTest>("testLab${appVariant.taskName()}${testConfig.taskName()}Test") {
            description =
                "Runs the ${testConfig.testType.name.toLowerCase()} test '${testConfig.name}' " +
                    "for the ${appVariant.name} build on Firebase Test Lab."
            group = JavaBasePlugin.VERIFICATION_GROUP
            dependsOn(uploadApks)
            appPackageId.set(appVariant.applicationId)
            google.set(extension.googleApi)
            prefix.set(extension.prefix)
            this.testConfig.set(testConfig)
            outputDir.set(file("$buildDir/test-results/$name"))
        }
    }
}

internal fun BaseVariant.taskName(): String = name.capitalize()
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

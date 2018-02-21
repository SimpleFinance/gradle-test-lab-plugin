package com.simple.gradle.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.api.TestVariant
import com.simple.gradle.testlab.internal.DefaultInstrumentationTest
import com.simple.gradle.testlab.internal.DefaultRoboTest
import com.simple.gradle.testlab.internal.DefaultTestConfigContainer
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.UploadResults
import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.tasks.TestLabTest
import com.simple.gradle.testlab.tasks.UploadApk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.reflect.Instantiator
import org.gradle.kotlin.dsl.configure
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class TestLabPlugin @Inject constructor(
    private val instantiator: Instantiator
): Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            pluginManager.apply("com.android.application")

            val testsContainer = instantiator.newInstance(DefaultTestConfigContainer::class.java, instantiator)
            testsContainer.registerBinding(InstrumentationTest::class.java, DefaultInstrumentationTest::class.java)
            testsContainer.registerBinding(RoboTest::class.java, DefaultRoboTest::class.java)

            val extension = instantiator.newInstance(TestLabExtension::class.java, testsContainer)
            extensions.add("testLab", extension)

            configure<AppExtension> {
                applicationVariants.all {
                    project.addTestLabTasksForApplicationVariant(extension, this)
                }
                testVariants.all {
                    project.addTestLabTasksForTestVariant(extension, this)
                }
            }
        }
    }
}

private fun Project.addTestLabTasksForApplicationVariant(extension: TestLabExtension, variant: ApplicationVariant) {
    variant.onFirstOutput { output ->
        val uploadResults = UploadResults()
        val uploadApp = maybeCreateUploadTask(
            "testLabUpload${variant.taskName()}AppApk", extension, variant, output, uploadResults)

        extension.tests
            .map { it as TestConfigInternal }
            .filterNot { it.requiresTestApk }
            .forEach { testConfig ->
                createDefaultTestLabTask(extension, testConfig, variant, output, uploadResults) {
                    dependsOn(uploadApp)
                }
            }
    }
}

private fun Project.addTestLabTasksForTestVariant(extension: TestLabExtension, variant: TestVariant) {
    variant.onFirstOutput { testOutput ->
        val uploadResults = UploadResults()
        val uploadTest = maybeCreateUploadTask(
            "testLabUpload${variant.taskName()}TestApk", extension, variant, testOutput, uploadResults)

        variant.testedVariant.onFirstOutput { appOutput ->
            val uploadApp = maybeCreateUploadTask(
                "testLabUpload${variant.taskName()}AppApk", extension, variant.testedVariant, appOutput, uploadResults)

            extension.tests
                .map { it as TestConfigInternal }
                .filter { it.requiresTestApk }
                .forEach { testConfig ->
                    createDefaultTestLabTask(extension, testConfig, variant.testedVariant, appOutput, uploadResults) {
                        dependsOn(uploadApp)
                        dependsOn(uploadTest)
                        testApk.set(testOutput.outputFile)
                        testPackageId.set(variant.applicationId)
                    }
                }
        }
    }
}

private fun Project.maybeCreateUploadTask(
    name: String,
    extension: TestLabExtension,
    variant: BaseVariant,
    output: BaseVariantOutput,
    uploadResults: UploadResults
): Task =
    tasks.findByName(name) ?: tasks.create(name, UploadApk::class.java) {
        val outputType = if (variant is TestVariant) "test" else "app"
        description = "Upload the ${variant.name} $outputType APK to Firebase Test Lab."
        dependsOn(variant.assemble)
        file.set(output.outputFile)
        prefix.set(extension.prefix)
        google.set(extension.googleApi)
        results = uploadResults
    }

private fun Project.createDefaultTestLabTask(
    extension: TestLabExtension,
    testConfig: TestConfigInternal,
    variant: BaseVariant,
    output: BaseVariantOutput,
    uploadResults: UploadResults,
    configure: TestLabTest.() -> Unit
): TestLabTest {
    val taskName = "testLab${variant.taskName()}${testConfig.taskName()}Test"
    return tasks.create(taskName, TestLabTest::class.java) {
        description =
            "Runs the ${testConfig.testType.name.toLowerCase()} test '${testConfig.name}' for the ${variant.name} build on Firebase Test Lab."
        appApk.set(output.outputFile)
        appPackageId.set(variant.applicationId)
        google.set(extension.googleApi)
        this.testConfig.set(testConfig)
        outputDir.set(file("$buildDir/test-results/$name"))
        prefix = extension.prefix
        this.uploadResults = uploadResults
        configure(this)
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
        .replace(Regex("\\s+(\\S)"), { result: MatchResult -> result.groups[1]!!.value.capitalize() })
        .let { Constants.FORBIDDEN_CHARACTERS.fold(it, { name, c -> name.replace(c, Constants.REPLACEMENT_CHARACTER) })}

private object Constants {
    val FORBIDDEN_CHARACTERS = charArrayOf(' ', '/', '\\', ':', '<', '>', '"', '?', '*', '|')
    const val REPLACEMENT_CHARACTER = '_'
}

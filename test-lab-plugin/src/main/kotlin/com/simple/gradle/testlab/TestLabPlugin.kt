@file:Suppress("UnstableApiUsage")

package com.simple.gradle.testlab

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.component.AndroidTestProperties
import com.android.build.api.component.ComponentIdentity
import com.android.build.api.component.ComponentProperties
import com.android.build.api.variant.ApplicationVariantProperties
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.simple.gradle.testlab.internal.DefaultTestLabExtension
import com.simple.gradle.testlab.internal.TestConfigInternal
import com.simple.gradle.testlab.internal.TestLabExtensionInternal
import com.simple.gradle.testlab.internal.tasks.UploadTask
import com.simple.gradle.testlab.model.FileType
import com.simple.gradle.testlab.model.RoboTest
import com.simple.gradle.testlab.model.TestLabExtension
import com.simple.gradle.testlab.tasks.ShowCatalog
import com.simple.gradle.testlab.tasks.TestLabTest
import com.simple.gradle.testlab.tasks.UploadApk
import com.simple.gradle.testlab.tasks.UploadBundle
import com.simple.gradle.testlab.tasks.UploadFiles
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.namedDomainObjectList
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

@Suppress("unused", "UnstableApiUsage")
class TestLabPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val extension = objects.newInstance<DefaultTestLabExtension>()
        extensions.add(TestLabExtension::class.java, TestLabExtension.NAME, extension)

        pluginManager.withPlugin("com.android.application") {
            val variants = objects.namedDomainObjectList(Variant::class)
            the<BaseAppModuleExtension>().onVariants {
                if (enabled) {
                    onProperties {
                        val app = Variant.App(
                            this,
                            addUploadAppApkTask(extension, this),
                            addUploadAppBundleTask(extension, this)
                        )
                        variants.add(app)
                        androidTestProperties {
                            variants.add(Variant.Test(this, app, addUploadTestApkTask(extension, this)))
                        }
                    }
                }
            }

            extension.testConfigs.all {
                val testConfig = this as TestConfigInternal
                val uploadExtraFiles = addUploadExtraFilesTask(extension, testConfig)

                if (testConfig.requiresTestApk) {
                    variants.matching { it is Variant.Test }.all {
                        addTestTasks(extension, testConfig, this, uploadExtraFiles)
                    }
                } else {
                    variants.matching { it is Variant.App }.all {
                        addTestTasks(extension, testConfig, this, uploadExtraFiles)
                    }
                }
            }

            addCatalogTask(extension)
        }
    }
}

private fun Project.addUploadExtraFilesTask(
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal
): TaskProvider<UploadFiles> = addUploadTask(extension, testConfig.name.asValidTaskName(), "extraFiles") {
    description = "Uploads extra files for ${testConfig.description()} to Firebase Test Lab."
    additionalApks.setFrom(testConfig.additionalApks)
    deviceFiles.addAll(testConfig.files)
    if (testConfig is RoboTest) {
        roboScript.set(testConfig.script)
    }
}

private fun Project.addUploadAppApkTask(
    extension: TestLabExtensionInternal,
    variant: ApplicationVariantProperties
): TaskProvider<UploadApk> = addUploadTask(extension, variant.name, "appApk") {
    description = "Uploads the app APK for '${variant.name}' to Firebase Test Lab."
    val apk = variant.artifacts.get(ArtifactType.APK)
    dependsOn(apk)
    fileType.set(FileType.APP_APK)
    apkDirectory.set(apk)
    artifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
}

private fun Project.addUploadAppBundleTask(
    extension: TestLabExtensionInternal,
    variant: ApplicationVariantProperties
): TaskProvider<UploadBundle> = addUploadTask(extension, variant.name, "appBundle") {
    description = "Uploads the app bundle for '${variant.name}' to Firebase Test Lab."
    val bundle = variant.artifacts.get(ArtifactType.BUNDLE)
    dependsOn(bundle)
    appBundle.set(bundle)
}

private fun Project.addUploadTestApkTask(
    extension: TestLabExtensionInternal,
    variant: AndroidTestProperties
): TaskProvider<UploadApk> = addUploadTask(extension, variant.testedVariant.name, "testApk") {
    description = "Uploads the test APK for '${variant.name}' to Firebase Test Lab."
    val apk = variant.artifacts.get(ArtifactType.APK)
    dependsOn(apk)
    fileType.set(FileType.TEST_APK)
    apkDirectory.set(apk)
    artifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
}

private inline fun <reified T : UploadTask> Project.addUploadTask(
    extension: TestLabExtensionInternal,
    taskName: String,
    suffix: String,
    crossinline config: T.() -> Unit
): TaskProvider<T> {
    return tasks.register<T>("testLab${taskName.capitalize()}Upload${suffix.capitalize()}") {
        prefix.set(extension.prefix)
        googleApiConfig.set(extension.googleApi)
        config()
    }
}

private fun Project.addTestTasks(
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal,
    variant: Variant,
    uploadExtraFiles: TaskProvider<UploadFiles>
) {
    val appVariant: Variant.App = when (variant) {
        is Variant.App -> variant
        is Variant.Test -> variant.app
    }
    val testVariant = variant as? Variant.Test

    // Skip "Apk" suffix for backward-compatibility
    addTestTask(
        extension,
        testConfig,
        appVariant.properties,
        "",
        listOfNotNull(appVariant.uploadApk, testVariant?.uploadApk, uploadExtraFiles)
    )

    addTestTask(
        extension,
        testConfig,
        appVariant.properties,
        "bundle",
        listOfNotNull(appVariant.uploadBundle, testVariant?.uploadApk, uploadExtraFiles)
    )
}

private fun Project.addTestTask(
    extension: TestLabExtensionInternal,
    testConfig: TestConfigInternal,
    variant: ApplicationVariantProperties,
    suffix: String,
    uploadTasks: List<TaskProvider<out UploadTask>>
) {
    tasks.register<TestLabTest>(
        "testLab${taskName(variant, testConfig.name)}${suffix.capitalize()}Test"
    ) {
        description =
            "Runs ${testConfig.description()} for the ${variant.name} build on Firebase Test Lab."
        group = JavaBasePlugin.VERIFICATION_GROUP

        dependsOn(uploadTasks)
        mustRunAfter(uploadTasks)
        appFileMetadata.from(uploadTasks.map { task -> task.map { it.results } })

        appPackageId.set(variant.applicationId)
        googleApiConfig.set(extension.googleApi)
        prefix.set(extension.prefix)
        this.testConfig.set(testConfig)
    }
}

private fun Project.addCatalogTask(extension: TestLabExtensionInternal) {
    tasks.register<ShowCatalog>("testLabCatalog") {
        googleApi.set(extension.googleApi)
    }
}

private fun TestConfigInternal.description(): String = "${testType.name.toLowerCase()} test '$name'"

private fun taskName(
    variant: ComponentIdentity,
    testName: String
): String = "${variant.taskName()}${testName.asValidTaskName().capitalize()}"

internal fun ComponentIdentity.taskName(): String = name.capitalize()

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

private sealed class Variant(
    val component: ComponentProperties
) : Named by component {

    data class App(
        val properties: ApplicationVariantProperties,
        val uploadApk: TaskProvider<UploadApk>,
        val uploadBundle: TaskProvider<UploadBundle>
    ) : Variant(properties)

    data class Test(
        val properties: AndroidTestProperties,
        val app: App,
        val uploadApk: TaskProvider<UploadApk>
    ) : Variant(properties)
}

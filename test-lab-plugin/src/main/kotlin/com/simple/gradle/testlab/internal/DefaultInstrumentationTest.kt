package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidInstrumentationTest
import com.google.api.services.testing.model.AppBundle
import com.google.api.services.testing.model.EnvironmentVariable
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.FileType
import com.simple.gradle.testlab.model.InstrumentationArtifactsHandler
import com.simple.gradle.testlab.model.InstrumentationTest
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultInstrumentationTest @Inject constructor(
    name: String,
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractTestConfig(TestType.INSTRUMENTATION, name, objects, providers),
    InstrumentationTest {

    private val artifactsHandler by lazy {
        DefaultInstrumentationArtifactsHandler(artifacts)
    }

    override val environmentVariables = objects.mapProperty<String, String>().empty()
    override val testRunnerClass = objects.property<String>()
    override val useOrchestrator = objects.property<Boolean>()
    override val testTargets = objects.listProperty<String>()

    override val requiresTestApk: Boolean = true

    override fun artifacts(configure: Action<in InstrumentationArtifactsHandler>) =
        configure.execute(artifactsHandler)

    override fun targetPackage(packageName: String) = testTargets.add("package $packageName")

    override fun targetClass(className: String) = testTargets.add("class $className")

    override fun targetMethod(className: String, methodName: String) =
        testTargets.add("class $className#$methodName")

    override fun TestSpecification.configure(
        files: List<AppFile>
    ): TestSpecification = setAndroidInstrumentationTest(
        AndroidInstrumentationTest().apply {
            val appApkFile = files.firstOrNull { it.type == FileType.APP_APK }
            val appBundleFile = files.firstOrNull { it.type == FileType.APP_BUNDLE }
            val testApkFile = files.firstOrNull { it.type == FileType.TEST_APK }
            when {
                appApkFile != null -> appApk = appApkFile.path
                appBundleFile != null -> appBundle = AppBundle().setBundleLocation(appBundleFile.path)
                else -> throw IllegalStateException("The application .apk or .abb file is required for test '$name'.")
            }
            testApk = checkNotNull(testApkFile?.path) { "The test .apk file is required for test '$name'." }
            appPackageId = this@DefaultInstrumentationTest.appPackageId.orNull
            testRunnerClass = this@DefaultInstrumentationTest.testRunnerClass.orNull
            testTargets = this@DefaultInstrumentationTest.testTargets.get()
            orchestratorOption = when (useOrchestrator.orNull) {
                null -> "ORCHESTRATOR_OPTION_UNSPECIFIED"
                false -> "DO_NOT_USE_ORCHESTRATOR"
                true -> "USE_ORCHESTRATOR"
            }
        }
    )

    override fun TestSetup.configure(): TestSetup =
        setEnvironmentVariables(
            this@DefaultInstrumentationTest.environmentVariables.get()
                .map { (key, value) -> EnvironmentVariable().setKey(key).setValue(value) }
        )
}

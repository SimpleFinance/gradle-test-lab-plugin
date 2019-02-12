package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidInstrumentationTest
import com.google.api.services.testing.model.EnvironmentVariable
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.InstrumentationArtifactsHandler
import com.simple.gradle.testlab.model.InstrumentationTest
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultInstrumentationTest @Inject constructor(
    name: String,
    layout: ProjectLayout,
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractTestConfig(TestType.INSTRUMENTATION, name, layout, objects, providers),
    InstrumentationTest {

    private val artifactsHandler by lazy {
        DefaultInstrumentationArtifactsHandler(artifacts)
    }

    override val environmentVariables = objects.mapProperty<String, String>()
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
        appApk: FileReference,
        testApk: FileReference?
    ): TestSpecification = setAndroidInstrumentationTest(AndroidInstrumentationTest()
        .setAppApk(appApk)
        .setTestApk(checkNotNull(testApk) { "Test APK not provided for test '$name'." })
        .setTestRunnerClass(testRunnerClass.orNull)
        .setTestTargets(testTargets.get())
        .setOrchestratorOption(when (useOrchestrator.orNull) {
            null -> "ORCHESTRATOR_OPTION_UNSPECIFIED"
            false -> "DO_NOT_USE_ORCHESTRATOR"
            true -> "USE_ORCHESTRATOR"
        }))

    override fun TestSetup.configure(): TestSetup =
        setEnvironmentVariables(this@DefaultInstrumentationTest.environmentVariables.get()
            .map { (key, value) -> EnvironmentVariable().setKey(key).setValue(value) })
}

package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidInstrumentationTest
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.InstrumentationArtifactsHandler
import com.simple.gradle.testlab.model.InstrumentationTest
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.Serializable
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultInstrumentationTest @Inject constructor(
    objects: ObjectFactory,
    private val providers: ProviderFactory,
    name: String
) : AbstractTestConfig(name, TestType.INSTRUMENTATION, objects, providers),
    InstrumentationTest,
    Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L
    }

    private val artifactsHandler by lazy {
        DefaultInstrumentationArtifactsHandler(artifacts)
    }

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

    override fun buildTestSpecification(
        appApk: FileReference,
        testApk: FileReference?
    ): Provider<TestSpecification> {
        return providers.provider {
            TestSpecification().setAndroidInstrumentationTest(
                AndroidInstrumentationTest()
                    .setAppApk(appApk)
                    .setTestApk(testApk)
                    .setTestRunnerClass(testRunnerClass.orNull)
                    .setTestTargets(testTargets.get())
                    .setOrchestratorOption(useOrchestrator.orNull.toOrchestratorOption()))
        }
    }

    private fun Boolean?.toOrchestratorOption(): String = when (this) {
        null -> "ORCHESTRATOR_OPTION_UNSPECIFIED"
        false -> "DO_NOT_USE_ORCHESTRATOR"
        true -> "USE_ORCHESTRATOR"
    }
}

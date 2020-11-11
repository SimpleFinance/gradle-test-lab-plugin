package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidInstrumentationTest
import com.google.api.services.testing.model.AppBundle
import com.google.api.services.testing.model.EnvironmentVariable
import com.google.api.services.testing.model.ManualSharding
import com.google.api.services.testing.model.ShardingOption
import com.google.api.services.testing.model.TestSetup
import com.google.api.services.testing.model.TestSpecification
import com.google.api.services.testing.model.TestTargetsForShard
import com.google.api.services.testing.model.UniformSharding
import com.simple.gradle.testlab.model.FileType
import com.simple.gradle.testlab.model.InstrumentationArtifactsHandler
import com.simple.gradle.testlab.model.InstrumentationTest
import com.simple.gradle.testlab.model.TestTargetsHandler
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("OverridingDeprecatedMember", "UnstableApiUsage")
internal open class DefaultInstrumentationTest @Inject constructor(
    name: String,
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractTestConfig(TestType.INSTRUMENTATION, name, objects, providers),
    InstrumentationTest {

    private val artifactsHandler: DefaultInstrumentationArtifactsHandler by lazy {
        DefaultInstrumentationArtifactsHandler(artifacts)
    }

    override val environmentVariables = objects.mapProperty<String, String>().empty()
    override val testRunnerClass = objects.property<String>()
    override val useOrchestrator = objects.property<Boolean>()
    override val testTargets = objects.listProperty<String>()

    override val targets: DefaultTestTargetsHandler = objects.newInstance()

    override val requiresTestApk: Boolean = true

    override fun artifacts(configure: Action<in InstrumentationArtifactsHandler>) {
        configure.execute(artifactsHandler)
    }

    override fun targets(configure: Action<in TestTargetsHandler>) {
        configure.execute(targets)
    }

    @Suppress("Deprecation")
    override fun targetPackage(packageName: String) = testTargets.add("package $packageName")

    @Suppress("Deprecation")
    override fun targetClass(className: String) = testTargets.add("class $className")

    @Suppress("Deprecation")
    override fun targetMethod(className: String, methodName: String) =
        testTargets.add("class $className#$methodName")

    @Suppress("Deprecation")
    override fun TestSpecification.configure(
        files: List<AppFile>
    ): TestSpecification = apply {
        environmentVariables.finalizeValue()
        testRunnerClass.finalizeValue()
        useOrchestrator.finalizeValue()
        testTargets.finalizeValue()

        androidInstrumentationTest = AndroidInstrumentationTest().apply {
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
            orchestratorOption = when (useOrchestrator.orNull) {
                null -> "ORCHESTRATOR_OPTION_UNSPECIFIED"
                false -> "DO_NOT_USE_ORCHESTRATOR"
                true -> "USE_ORCHESTRATOR"
            }
            testTargets = this@DefaultInstrumentationTest.testTargets.get() + targets.targets.get()

            val shardCount = targets.shardCount.orNull
            val shards = targets.shards.get().takeUnless { it.isEmpty() }

            check(shardCount == null || shards == null) {
                "Cannot set both 'shardCount' and 'shards' for test '$name'."
            }

            shardingOption = when {
                shardCount != null -> ShardingOption().setUniformSharding(
                    UniformSharding().setNumShards(targets.shardCount.get())
                )
                shards != null -> ShardingOption().setManualSharding(
                    ManualSharding().setTestTargetsForShard(
                        shards.map { shard ->
                            TestTargetsForShard().setTestTargets(shard.targets.get())
                        }
                    )
                )
                else -> null
            }
        }
    }

    override fun TestSetup.configure(): TestSetup =
        setEnvironmentVariables(
            this@DefaultInstrumentationTest.environmentVariables.get()
                .map { (key, value) -> EnvironmentVariable().setKey(key).setValue(value) }
        )
}

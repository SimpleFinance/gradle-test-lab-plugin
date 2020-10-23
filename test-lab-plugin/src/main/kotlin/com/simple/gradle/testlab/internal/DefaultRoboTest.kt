package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.AndroidRoboTest
import com.google.api.services.testing.model.AppBundle
import com.google.api.services.testing.model.TestSpecification
import com.simple.gradle.testlab.model.FileType
import com.simple.gradle.testlab.model.RoboArtifactsHandler
import com.simple.gradle.testlab.model.RoboDirective
import com.simple.gradle.testlab.model.RoboDirectivesHandler
import com.simple.gradle.testlab.model.RoboTest
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject
import com.google.api.services.testing.model.RoboDirective as GoogleRoboDirective

@Suppress("UnstableApiUsage")
internal open class DefaultRoboTest @Inject constructor(
    name: String,
    objects: ObjectFactory,
    providers: ProviderFactory
) : AbstractTestConfig(TestType.ROBO, name, objects, providers),
    RoboTest {

    private val artifactsHandler by lazy {
        DefaultRoboArtifactsHandler(artifacts)
    }

    private val roboDirectivesHandler by lazy {
        DefaultRoboDirectivesHandler(directives)
    }

    override val directives = objects.listProperty<RoboDirective>()
    override val appInitialActivity = objects.property<String>()
    override val maxDepth = objects.property<Int>()
    override val maxSteps = objects.property<Int>()

    override val requiresTestApk: Boolean = false

    override fun artifacts(configure: Action<in RoboArtifactsHandler>) =
        configure.execute(artifactsHandler)

    override fun directives(configure: Action<in RoboDirectivesHandler>) =
        configure.execute(roboDirectivesHandler)

    override fun TestSpecification.configure(files: List<AppFile>): TestSpecification = setAndroidRoboTest(
        AndroidRoboTest().apply {
            val appApkFile = files.find { it.type == FileType.APP_APK }
            val appBundleFile = files.find { it.type == FileType.APP_BUNDLE }
            when {
                appApkFile != null -> appApk = appApkFile.path
                appBundleFile != null -> appBundle = AppBundle().setBundleLocation(appBundleFile.path)
                else -> throw IllegalStateException("The application .apk or .abb file is required for test '$name'.")
            }
            appInitialActivity = this@DefaultRoboTest.appInitialActivity.orNull
            maxDepth = this@DefaultRoboTest.maxDepth.orNull
            maxSteps = this@DefaultRoboTest.maxSteps.orNull
            roboDirectives = directives.orNull?.map {
                GoogleRoboDirective()
                    .setActionType(it.actionType)
                    .setResourceName(it.resourceName)
                    .setInputText(it.inputText)
            }
        }
    )
}

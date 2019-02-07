import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_android_tools_build_gradle: String = "3.3.1"

    const val google_api_client: String = "1.28.0"

    const val google_api_services_storage: String = "v1-rev20181109-1.28.0"

    const val google_api_services_testing: String = "v1-rev20190107-1.28.0"

    const val google_api_services_toolresults: String = "v1beta3-rev20181112-1.28.0"

    const val com_gradle_plugin_publish_gradle_plugin: String = "0.10.1"

    const val hamkrest: String = "1.7.0.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val junit: String = "4.12" 

    const val org_gradle_kotlin_kotlin_dsl_gradle_plugin: String = "1.2.1"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.21"

    const val kotlin_annotation_processing_gradle: String = "1.3.21"

    const val kotlin_reflect: String = "1.3.21"

    const val kotlin_stdlib_jdk8: String = "1.3.21"

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "4.8"

        const val currentVersion: String = "5.2"

        const val nightlyVersion: String = "5.3-20190207000055+0000"

        const val releaseCandidate: String = ""
    }
}

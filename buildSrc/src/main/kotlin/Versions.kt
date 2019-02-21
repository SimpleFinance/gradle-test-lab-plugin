import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_android_tools_build_gradle: String = "3.3.1" 

    const val com_github_johnrengelman_shadow_gradle_plugin: String = "4.0.4" 

    const val google_api_client: String = "1.28.0" 

    const val google_api_services_storage: String = "v1-rev20181109-1.28.0" 

    const val google_api_services_testing: String = "v1-rev20190107-1.28.0" 

    const val google_api_services_toolresults: String = "v1beta3-rev20181112-1.28.0" 

    const val com_gradle_plugin_publish_gradle_plugin: String = "0.10.1" 

    const val hamkrest: String = "1.7.0.0" 

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val junit: String = "4.12" 

    const val org_gradle_kotlin_kotlin_dsl_gradle_plugin: String = "1.1.3" // available: "1.2.2"

    const val org_jetbrains_dokka_gradle_plugin: String = "0.9.17" 

    const val org_jetbrains_kotlin: String = "1.3.20" // available: "1.3.21"

    const val kotlinx_serialization_runtime: String = "0.10.0" 

    const val org_jmailen_kotlinter_gradle_plugin: String = "1.21.0" 

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "5.2.1"

        const val currentVersion: String = "5.2.1"

        const val nightlyVersion: String = "5.3-20190211022529+0000"

        const val releaseCandidate: String = ""
    }
}

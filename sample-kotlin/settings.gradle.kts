pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
    }
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id == "com.android.application" ->
                    useModule("com.android.tools.build:gradle:${requested.version}")
                requested.id.id == "com.simple.gradle.testlab" ->
                    useModule("com.simple.gradle.testlab:test-lab-plugin:${requested.version}")
            }
        }
    }
}

rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "sample-kotlin"

includeBuild("..")
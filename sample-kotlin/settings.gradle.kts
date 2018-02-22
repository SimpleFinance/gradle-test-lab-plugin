pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            } else if (requested.id.id == "com.simple.gradle.testlab") {
                useModule("com.simple.gradle.testlab:test-lab-plugin:${requested.version}")
            }
        }
    }
}

rootProject.buildFileName = "build.gradle.kts"

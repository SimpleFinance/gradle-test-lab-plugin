plugins {
    id("com.android.application") version "3.3.1"
    id("com.simple.gradle.testlab")
}

android {
    compileSdkVersion(28)
    buildToolsVersion("28.0.3")
}

repositories {
    google()
    jcenter()
}

dependencies {
    androidTestImplementation("androidx.test:core:1.0.0")
    androidTestImplementation("androidx.test:runner:1.1.0")
    androidTestImplementation("androidx.test:rules:1.1.0")
}

testLab {
    googleApi {
        serviceCredentials.set(file("../dummy-credentials.json"))
        projectId.set("test-project-id-12345")
    }
    tests {
        robo("foo") {
            device("sailfish", 26)
        }
        instrumentation("instrumented") {
            device("sailfish", 26)
        }
    }
}

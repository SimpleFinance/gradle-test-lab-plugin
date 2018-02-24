plugins {
    id("com.android.application") version "3.0.1"
    id("com.simple.gradle.testlab")
}

android {
    compileSdkVersion(27)
    buildToolsVersion("27.0.3")
}

repositories {
    google()
    jcenter()
}

dependencies {
    androidTestImplementation("com.android.support:support-annotations:27.0.2")
    androidTestImplementation("com.android.support.test:runner:1.0.1")
    androidTestImplementation("com.android.support.test:rules:1.0.1")
}

testLab {
    tests {
        robo("foo") {
            device {
                modelId = "sailfish"
                version = 26
            }
            artifacts {
                all()
            }
        }
        instrumentation("instrumented") {
            device {
                modelId = "sailfish"
                version = 26
            }
            artifacts {
                junit = true
            }
        }
    }
}

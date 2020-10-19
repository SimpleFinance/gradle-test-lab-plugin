import com.simple.gradle.testlab.model.Orientation
import com.simple.gradle.testlab.model.TestConfig

plugins {
    id("com.android.application") version "4.0.0"
    id("com.simple.gradle.testlab")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")
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
        bucketName = "my-bucket-name"
        serviceCredentials = file("/path/to/service/credentials.json")
        projectId = "my-project-id-12345"
    }

    tests {
        instrumentation("instrumented") {
            common()

            environmentVariables.put("key", "value")
            testRunnerClass.set("com.example.test.MyTestRunner")
            testTargets.addAll(
                "package com.my.package.name",
                "notPackage com.package.to.skip",
                "class com.foo.ClassName",
                "notClass com.foo.ClassName#testMethodToSkip",
                "annotation com.foo.AnnotationToRun",
                "size large notAnnotation com.foo.AnnotationToSkip"
            )
            artifacts {
                all()
                instrumentation = true
                junit = true
                logcat = true
                video = true
            }
        }

        robo("foo") {
            common()

            appInitialActivity.set("com.example.app.MainActivity")
            maxDepth.set(50)
            maxSteps.set(200)

            artifacts {
                all()
                logcat = true
                screenshots = true
                video = true
            }

            directives {
                click(resourceName = "login_button")
                text(resourceName = "username", inputText = "alice")
            }
        }
    }
}

fun TestConfig.common() {
    additionalApks.from(
        "/path/to/some.apk",
        "/path/to/another.apk"
    )
    disablePerformanceMetrics.set(true)
    disableVideoRecording.set(true)
    resultsHistoryName.set(provider {
        val prNumber = "1234"
        "GitHub PR $prNumber"
    })
    testTimeout.set("600s")
    account.googleAuto()
    directoriesToPull.addAll(
        "/sdcard/path/to/dir",
        "/data/local/tmp/path/to/dir"
    )
    networkProfile.set("LTE")
    device(
        model = "hammerhead",
        api = 21,
        locale = "en",
        orientation = Orientation.PORTRAIT
    )
    files {
        obb(source = file("/path/to/some.obb"), filename = "main.0300110.com.example.android.obb")
        push(source = file("/path/to/some.file"), devicePath = "/sdcard/some.file")
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.6.1"
}


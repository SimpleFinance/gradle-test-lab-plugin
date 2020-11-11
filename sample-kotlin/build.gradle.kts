import com.simple.gradle.testlab.model.Orientation
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestSize

plugins {
    id("com.android.application") version "4.1.0"
    id("com.simple.gradle.testlab")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(23)
        versionCode(1)
        versionName("0.1")
    }

    splits {
        density {
            isEnable = true
            compatibleScreens("small", "normal", "large", "xlarge")
        }
    }
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
            targets {
                packages.addAll("com.my.package.name", "!com.my.package.other")
                classes.addAll("com.foo.ClassName", "!com.foo.OtherClass")
                annotations.addAll("com.foo.AnnotationToRun", "!com.foo.AnnotationToSkip")
                includeFile.set("/data/local/tmp/include.txt")
                excludeFile.set("/data/local/tmp/exclude.txt")
                regex.set("""/^com\.package\.(MyClass|OtherClass)#test.+""")
                size.set(TestSize.LARGE)
                shardCount.set(10)
                shard {
                    packages.addAll("com.my.package.name", "!com.my.package.other")
                    classes.addAll("com.foo.ClassName", "!com.foo.OtherClass")
                    annotations.addAll("com.foo.AnnotationToRun", "!com.foo.AnnotationToSkip")
                    includeFile.set("/data/local/tmp/include.txt")
                    excludeFile.set("/data/local/tmp/exclude.txt")
                    regex.set("""/^com\.package\.(MyClass|OtherClass)#test.+""")
                    size.set(TestSize.LARGE)
                }
            }
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

            script.set(file("script.robo"))

            startingIntents {
                launcherActivity()
                launcherActivity {
                    timeout.set(10)
                }
                startActivity {
                    action.set("android.intent.action.VIEW")
                    categories.add("android.intent.category.TEST")
                    uri.set("https://www.example.com")
                    timeout.set(20)
                }
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
    dontAutograntPermissions.set(true)
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
    systrace {
        enabled.set(true)
        durationSeconds.set(30)
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.6.1"
}

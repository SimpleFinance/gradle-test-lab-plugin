# Firebase Test Lab plugin for Gradle

Run Android tests on Firebase directly from your Gradle build.

## Table of Contents

- [Compatibility](#compatibility)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Support](#support)
- [Contributing](#contributing)
- [License](#license)

## Compatibility

The following lists the configurations that are supported and tested. Other configurations may
work, but are not supported.

| Plugin version | Gradle version | Android Gradle version |
| -------------- | -------------- | ---------------------- |
| 0.3            | 5.2.1          | 3.3.1                  |

## Installation

### `plugins` DSL

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
    id("com.simple.gradle.testlab") version "$pluginVersion"
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'com.simple.gradle.testlab' version "$pluginVersion"
}
```

</details>

### `buildscript` block

<details open>
<summary>Kotlin</summary>

```kotlin
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.simple.gradle.testlab:test-lab-plugin:$pluginVersion")
    }
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath "com.simple.gradle.testlab:test-lab-plugin:$pluginVersion"
    }
}
```

</details>

## Usage

After applying the plugin, create an API configuration and define a test:

<details open>
<summary>Kotlin</summary>

```kotlin
testLab {
    googleApi {
        serviceCredentials.set(file("/path/to/service/credentials.json"))
        projectId.set("my-project-id-12345")
    }
    tests {
        robo("fuzz") {
            device(model = "sailfish", api = 21)
        }
    }
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
testLab {
    googleApi {
        serviceCredentials = file('/path/to/service/credentials.json')
        projectId = 'my-project-id-12345'
    }
    tests {
        robo("fuzz") {
            device {
                model = 'sailfish'
                api = 21
            }
        }
    }
}
```

</details>

The plugin will then create tasks for each Android build variant:

- `testLabReleaseFuzzTest`
- `testLabDebugFuzzTest`

Run the tests by calling the desired task:

```bash
./gradlew testLabDebugFuzzTest
```

## Configuration

<details open>
<summary>Kotlin</summary>

```kotlin
testLab {

    // Configures the Google API configuration for this project.
    googleApi {
        
        // The Google Cloud Storage bucket where the test results will be stored.
        // In this example, a `Provider` is used to only call `System.currentTimeMillis()`
        // at execution time.
        bucketName.set(provider { 
            val currentTime = System.currentTimeMillis()
            "my-bucket-$currentTime" 
        })
        
        // Path to service account credentials used to execute tests on Firebase and
        // fetch results from Google Cloud Storage. If not provided, application default 
        // credentials will be used.
        serviceCredentials.set(file("/path/to/service/credentials.json"))
        
        // The Firebase/Google Cloud Platform project to use when executing tests and
        // fetching results from Google Cloud Storage.
        projectId.set("my-project-id-12345")
    }
    
    // Configures the test configurations for this project.
    tests {
    
        // Configure and add an instrumentation test to this container.
        // See "Common test options" below for options available to all test types.
        instrumentation(name = "instrumentation") {
            
            // Environment variables to set for the test.
            environmentVariables.put("key", "value")
            
            // The `InstrumentationTestRunner` class. Optional; the default is determined by 
            // examining the application's manifest.
            testRunnerClass.set("com.example.test.MyTestRunner")
            
            // Test targets to execute. Optional; if empty, all targets in the module will
            // be ran.
            testTargets.addAll(
                "package com.my.package.name",
                "notPackage com.package.to.skip",
                "class com.foo.ClassName",
                "notClass com.foo.ClassName#testMethodToSkip",
                "annotation com.foo.AnnotationToRun",
                "size large notAnnotation com.foo.AnnotationToSkip"
            )
            
            // Configures artifacts to fetch after completing the test.
            artifacts {
            
                // Fetch all available artifacts for this test type.
                all()
            
                // Fetch instrumentation logs to `instrumentation.results`.
                instrumentation = true
                
                // Fetch JUnit test results to `test_result_$i.xml` for each result.
                junit = true
                
                // Fetch device logs to `logcat`.
                logcat = true
                
                // Fetch captured video to `video.mp4`.
                video = true
            }
        }
        
        // Configure and add a robo test to this container.
        // See "Common test options" below for options available to all test types.
        robo(name = "robo") {
            
            // The initial activity that should be used to start the app. Optional.
            appInitialActivity.set("com.example.app.MainActivity")
            
            // The max depth of the traversal stack Robo can explore. Needs to be at least `2` to 
            // make Robo explore the app beyond the first activity. Optional; the default is `50`.
            maxDepth.set(50)
            
            // The max number of steps Robo can execute. Optional; the default is no limit.
            maxSteps.set(200)
            
            // Configures artifacts to fetch after completing the test.
            artifacts {
            
                // Fetch all available artifacts for this test type.
                all()
                
                // Fetch device logs to `logcat`.
                logcat = true
                
                // Fetch captured screenshots to `screenshots/$filename.png` for each screenshot.
                screenshots = true
                
                // Fetch captured video to `video.mp4`.
                video = true
            }
            
            // Configures the [robo directives][RoboDirectivesHandler] for this test.
            directives {
                
                // Add a `CLICK` directive on the UI element for [resourceName].
                click(resourceName = "login_button")
                
                // Add a `TEXT` directive which inputs [inputText] on the UI element for 
                // [resourceName].
                text(resourceName = "username", inputText = "alice")
            }
        }
    }
}
```

</summary>

<details>
<summary>Groovy</summary>

```groovy
testLab {

    // Configures the Google API configuration for this project.
    googleApi {
        
        // The Google Cloud Storage bucket where the test results will be stored.
        // In this example, a `Provider` is used to only call `System.currentTimeMillis()`
        // at execution time.
        bucketName = provider {
            def currentTime = System.currentTimeMillis() 
            "my-bucket-$currentTime" 
        }
        
        // Path to service account credentials used to execute tests on Firebase and
        // fetch results from Google Cloud Storage. If not provided, application default 
        // credentials will be used.
        serviceCredentials = file("/path/to/service/credentials.json")
        
        // The Firebase/Google Cloud Platform project to use when executing tests and
        // fetching results from Google Cloud Storage.
        projectId = "my-project-id-12345"
    }
    
    // Configures the test configurations for this project.
    tests {
    
        // Configure and add an instrumentation test to this container.
        // See "Common test options" below for options available to all test types.
        instrumentation("instrumentation") {
            
            // Environment variables to set for the test.
            environmentVariables["key"] = "value"
            
            // The `InstrumentationTestRunner` class. Optional; the default is determined by 
            // examining the application's manifest.
            testRunnerClass = "com.example.test.MyTestRunner"
            
            // Test targets to execute. Optional; if empty, all targets in the module will
            // be ran.
            testTargets.addAll(
                "package com.my.package.name",
                "notPackage com.package.to.skip",
                "class com.foo.ClassName",
                "notClass com.foo.ClassName#testMethodToSkip",
                "annotation com.foo.AnnotationToRun",
                "size large notAnnotation com.foo.AnnotationToSkip"
            )
            
            // Configures artifacts to fetch after completing the test.
            artifacts {
            
                // Fetch all available artifacts for this test type.
                all()
            
                // Fetch instrumentation logs to `instrumentation.results`.
                instrumentation = true
                
                // Fetch JUnit test results to `test_result_$i.xml` for each result.
                junit = true
                
                // Fetch device logs to `logcat`.
                logcat = true
                
                // Fetch captured video to `video.mp4`.
                video = true
            }
        }
        
        // Configure and add a robo test to this container.
        // See "Common test options" below for options available to all test types.
        robo("robo") {
            
            // The initial activity that should be used to start the app. Optional.
            appInitialActivity = "com.example.app.MainActivity"
            
            // The max depth of the traversal stack Robo can explore. Needs to be at least `2` to 
            // make Robo explore the app beyond the first activity. Optional; the default is `50`.
            maxDepth = 50
            
            // The max number of steps Robo can execute. Optional; the default is no limit.
            maxSteps = 200
            
            // Configures artifacts to fetch after completing the test.
            artifacts {
            
                // Fetch all available artifacts for this test type.
                all()
                
                // Fetch device logs to `logcat`.
                logcat = true
                
                // Fetch captured screenshots to `screenshots/$filename.png` for each screenshot.
                screenshots = true
                
                // Fetch captured video to `video.mp4`.
                video = true
            }
            
            // Configures the [robo directives][RoboDirectivesHandler] for this test.
            directives {
                
                // Add a `CLICK` directive on the UI element for [resourceName].
                click("login_button")
                
                // Add a `TEXT` directive which inputs [inputText] on the UI element for 
                // [resourceName].
                text("username", "alice")
            }
        }
    }
}
```

</details>


#### Common test options

The following options apply to both `instrumentation` and `robo` tests.

<details open>
<summary>Kotlin</summary>

```kotlin
// APKs to install in addition to those being directly tested. Currently capped at 100.
additionalApks.from(
    "/path/to/some.apk",
    "/path/to/another.apk"
)

// Disables performance metrics recording; may reduce test latency.
disablePerformanceMetrics.set(true)

// Disables video recording; may reduce test latency.
disableVideoRecording.set(true)

// The name of the results history entry. This appears in the Firebase console and
// identifies this test.
// Here, a `Provider` is used to call a fictitious `getGithubPullRequestNumber` method at execution
// time.
resultsHistoryName.set(provider {
    val prNumber = getGithubPullRequestNumber()
    "GitHub PR $prNumber"
})

// Max time a test execution is allowed to run before it is automatically cancelled.
// Optional; the default is `5 min`.
testTimeout.set("600s")

// Sign in to an automatically-created Google account for the duration of this test.
autoGoogleAccount.set(true)

// List of directories on the device to upload to GCS at the end of the test; they must be
// absolute paths under /sdcard or /data/local/tmp. Path names are restricted to characters 
// `a-z` `0-9` `_` `-` `.` `+` and `/`.
directoriesToPull.addAll(
    "/sdcard/path/to/dir",
    "/data/local/tmp/path/to/dir"
)

// The network traffic profile used for running the test.
networkProfile.set("LTE")

// Configure and add a [device][Device] on which this test should run.
// Default values are shown below.
device(
    model = "hammerhead",
    api = 21,
    locale = "en",
    orientation = Orientation.PORTRAIT
)

// Configure the list of files to push to the device before starting the test.
files {
    
    // Opaque Binary Blob (OBB) file(s) to install on the device.
    //
    // source - path to the source OBB file
    // filename - OBB file name which must conform to the format as specified by Android, e.g.
    //     `[main|patch].0300110.com.example.android.obb`, which will be installed into
    //     `/Android/obb/` on the device.
    obb(source = file("/path/to/some.obb"), filename = "main.0300110.com.example.android.obb")
    
    // A file or directory to install on the device before the test starts.
    //
    // source - path to the source file
    // devicePath - Where to put the content on the device. Must be an absolute, whitelisted
    //     path. If the file exists, it will be replaced. The following device-side directories
    //     and any of their subdirectories are whitelisted:
    //
    //     - `${EXTERNAL_STORAGE}`
    //     - `/sdcard/${ANDROID_DATA}/local/tmp`
    //     - `/data/local/tmp`
    //
    //     Specifying a path outside of these directory trees is invalid.
    push(source = file("/path/to/some.file"), devicePath = "/sdcard/some.file")
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
// APKs to install in addition to those being directly tested. Currently capped at 100.
additionalApks.from(
    "/path/to/some.apk",
    "/path/to/another.apk"
)

// Disables performance metrics recording; may reduce test latency.
disablePerformanceMetrics = true

// Disables video recording; may reduce test latency.
disableVideoRecording = true

// The name of the results history entry. This appears in the Firebase console and
// identifies this test.
// Here, a `Provider` is used to call a fictitious `getGithubPullRequestNumber` method at execution
// time.
resultsHistoryName = provider {
    def prNumber = getGithubPullRequestNumber() 
    "GitHub PR $prNumber" 
}

// Max time a test execution is allowed to run before it is automatically cancelled.
// Optional; the default is `5 min`.
testTimeout = "600s"

// Sign in to an automatically-created Google account for the duration of this test.
autoGoogleLogin = true

// List of directories on the device to upload to GCS at the end of the test; they must be
// absolute paths under /sdcard or /data/local/tmp. Path names are restricted to characters 
// `a-z` `0-9` `_` `-` `.` `+` and `/`.
directoriesToPull.addAll(
    "/sdcard/path/to/dir",
    "/data/local/tmp/path/to/dir"
)

// The network traffic profile used for running the test.
networkProfile = "LTE"

// Configure and add a [device][Device] on which this test should run.
// Note: Add `import com.gradle.simple.testlab.model.Orientation` to the top of your script.
// Default values are shown below.
device {
    model = "hammerhead"
    api = 21
    locale = "en"
    orientation = Orientation.PORTRAIT
}
```

</details>

## Support

Please [open an issue](https://github.com/SimpleFinance/gradle-test-lab-plugin/issues/new) for 
support.

## Contributing

Pull requests are welcome. Please observe the following norms:

1. Read existing pull requests or issues to see if your change is still needed.
2. Open an issue explaining the use case for your change prior to submitting.

## License

```
Copyright 2017 Simple Finance Technology Corp

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
```
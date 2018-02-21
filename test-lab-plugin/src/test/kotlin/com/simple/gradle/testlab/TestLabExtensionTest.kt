package com.simple.gradle.testlab

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TestLabExtensionTest {
    @get:Rule val projectDir = TemporaryFolder()

    @Before
    fun setup() {
        val main = projectDir.newFolder("src", "main")
        val java = projectDir.newFolder("src", "main", "java", "example")
        val androidTest = projectDir.newFolder("src", "androidTest", "java", "example")

        File(main, "AndroidManifest.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="example">
                <application android:label="Minimal">
                    <activity android:name=".MainActivity">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
            """.trimIndent())

        File(java, "MainActivity.java").writeText("""
            package example;
            import android.app.Activity;
            import android.os.Bundle;
            import android.widget.TextView;
            public class MainActivity extends Activity {
                @Override
                public void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    TextView label = new TextView(this);
                    label.setText("Hello world!");
                    setContentView(label);
                }
            }
            """.trimIndent())

        File(androidTest, "BasicTest.java").writeText("""
            package example;
            import android.support.test.runner.AndroidJUnit4;
            @RunWith(AndroidJunit4.class)
            @SmallTest
            public class BasicTest {
              @Test
              public void hello() {
                System.out.println("Hello world!");
              }
            }
            """.trimIndent())
    }

    @Test
    fun `groovy dsl`() {
        groovyBuildFile().appendText("""
            import com.simple.gradle.testlab.model.RoboTest
            testLab {
              tests {
                foo(RoboTest) {
                  device { modelId = 'sailfish'; version = 26 }
                }
                instrumentation {
                  name = "instrumented"
                  device {
                    modelId = 'sailfish'
                    version = 26
                  }
                  artifacts { all() }
                }
              }
            }
            """.trimIndent())
        val result = GradleRunner.create()
            .withDebug(true)
            .withProjectDir(projectDir.root)
            .withArguments(":tasks", "--all", "--stacktrace")
            .withPluginClasspath()
            .build()
        assert.that(result.task(":tasks")?.outcome, equalTo(SUCCESS))
        assert.that(result.output, containsSubstring("testLabUploadDebugAppApk"))
        assert.that(result.output, containsSubstring("testLabUploadDebugTestApk"))
        assert.that(result.output, containsSubstring("testLabUploadReleaseAppApk"))
        assert.that(result.output, containsSubstring("testLabReleaseFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugInstrumentedTest"))
    }

    @Test
    fun `kotlin dsl`() {
        kotlinBuildFile().appendText("""
            testLab {
              tests {
                robo {
                  name = "foo"
                  device { modelId = "sailfish"; version = 26 }
                }
                instrumentation {
                  name = "instrumented"
                  device {
                    modelId = "sailfish"
                    version = 26
                  }
                  artifacts { all() }
                }
              }
            }
            """)
        val result = GradleRunner.create()
            .withDebug(true)
            .withProjectDir(projectDir.root)
            .withArguments(":kotlinDslAccessorsSnapshot")
            .withPluginClasspath()
            .build()

        assert.that(result.task(":tasks")?.outcome, equalTo(SUCCESS))
        assert.that(result.output, containsSubstring("testLabUploadDebugAppApk"))
        assert.that(result.output, containsSubstring("testLabUploadDebugTestApk"))
        assert.that(result.output, containsSubstring("testLabUploadReleaseAppApk"))
        assert.that(result.output, containsSubstring("testLabReleaseFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugFooTest"))
        assert.that(result.output, containsSubstring("testLabDebugInstrumentedTest"))
    }

    private fun groovyBuildFile(): File = projectDir.newFile("build.gradle").apply {
        appendText("""
            plugins { id 'com.simple.gradle.testlab' }
            android {
              compileSdkVersion 27
              buildToolsVersion '27.0.2'
              defaultConfig {
                testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'
              }
            }
            repositories {
              google()
            }
            dependencies {
              androidTestImplementation 'com.android.support:support-annotations:24.0.0'
              androidTestImplementation 'com.android.support.test:runner:0.5'
              androidTestImplementation 'com.android.support.test:rules:0.5'
            }

            """.trimIndent())
    }

    private fun kotlinBuildFile(): File = projectDir.newFile("build.gradle.kts").apply {
        appendText("""
            plugins {
              id("com.android.application") version "3.0.1"
              id("com.simple.gradle.testlab")
            }
            configure<com.android.build.gradle.AppExtension> {
              buildToolsVersion("27.0.2")
              compileSdkVersion(27)
              defaultConfig {
                testInstrumentationRunner("android.support.test.runner.AndroidJunitRunner")
              }
            }
            repositories {
              google()
            }
            dependencies {
              "androidTestImplementation"("com.android.support:support-annotations:24.0.0")
              "androidTestImplementation"("com.android.support.test:runner:0.5")
              "androidTestImplementation"("com.android.support.test:rules:0.5'")
            }

            """.trimIndent())
    }
}

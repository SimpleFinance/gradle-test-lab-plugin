import org.apache.maven.model.Dependency
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version Versions.com_gradle_plugin_publish_gradle_plugin
    `maven-publish`
}

group = rootProject.group
version = rootProject.version
description = "Run Android application tests on Firebase Test Lab"

repositories {
    jcenter()
    google()
}

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)
    implementation(Libs.com_android_tools_build_gradle)
    implementation(Libs.google_api_client) {
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation(Libs.google_api_services_storage)
    implementation(Libs.google_api_services_testing)
    implementation(Libs.google_api_services_toolresults)

    testRuntimeOnly(Libs.com_android_tools_build_gradle)
    testImplementation(Libs.kotlin_reflect)
    testImplementation(Libs.junit)
    testImplementation(Libs.hamkrest)
    testImplementation(gradleKotlinDsl())
}

gradlePlugin {
    plugins {
        create("testLab") {
            id = project.group as String
            implementationClass = "com.simple.gradle.testlab.TestLabPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/SimpleFinance/gradle-test-lab-plugin"
    vcsUrl = "$website.git"
    description = project.description
    tags = listOf("firebase", "test-lab", "android")

    (plugins) {
        "testLab" {
            id = project.group as String
            displayName = "Gradle Firebase Test Lab plugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "releases"
            url = uri("https://nexus-build.banksimple.com/repository/simple-maven-releases/")
            credentials {
                username = properties["nexusUsername"]?.toString()
                password = properties["nexusPasswordh"]?.toString()
            }
        }
        maven {
            name = "snapshots"
            url = uri("https://nexus-build.banksimple.com/repository/simple-maven-snapshots/")
            credentials {
                username = properties["nexusUsername"]?.toString()
                password = properties["nexusPassword"]?.toString()
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    dependsOn(rootProject.tasks["customInstallation"])
}

val SourceSet.kotlin: SourceDirectorySet
    get() = withConvention(KotlinSourceSet::class) { kotlin }

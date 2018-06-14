import org.apache.maven.model.Dependency
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl` version "0.18.0"
    kotlin("jvm") version "1.2.50"
    id("com.gradle.plugin-publish") version "0.9.10"
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
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.android.tools.build:gradle:3.1.0")
    implementation("com.google.api-client:google-api-client:1.23.0") {
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation("com.google.apis:google-api-services-storage:v1-rev134-1.23.0")
    implementation("com.google.apis:google-api-services-testing:v1-rev40-1.23.0")
    implementation("com.google.apis:google-api-services-toolresults:v1beta3-rev396-1.23.0")

    testRuntimeOnly("com.android.tools.build:gradle:3.1.0")
    testImplementation(kotlin("reflect"))
    testImplementation("junit:junit:4.12")
    testImplementation("com.natpryce:hamkrest:1.4.2.2")
    testImplementation(gradleKotlinDsl())
}

gradlePlugin {
    (plugins) {
        "testLab" {
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
                password = properties["nexusPassword"]?.toString()
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

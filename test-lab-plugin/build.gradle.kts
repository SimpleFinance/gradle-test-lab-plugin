import org.apache.maven.model.Dependency
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.1.51"
    id("com.gradle.plugin-publish") version "0.9.9"
}

group = rootProject.group
version = rootProject.version
description = "Run Android application tests on Firebase Test Lab"

repositories {
    jcenter()
    google()
}

dependencies {
    implementation(kotlin("stdlib-jre8", version = "1.1.51"))
    implementation("com.android.tools.build:gradle:3.0.0")
    implementation("com.google.api-client:google-api-client:1.23.0") {
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation("com.google.apis:google-api-services-toolresults:v1beta3-rev284-1.23.0")
    implementation("com.google.apis:google-api-services-storage:v1-rev115-1.23.0")
    implementation(project(":cloud-testing-api"))

    testRuntimeOnly("com.android.tools.build:gradle:3.0.0")
    testImplementation(kotlin("reflect", version = "1.1.51"))
    testImplementation("junit:junit:4.12")
    testImplementation("com.natpryce:hamkrest:1.4.2.2")
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

evaluationDependsOn(":cloud-testing-api")

val jar: Jar by tasks
project(":cloud-testing-api").let {
    jar.dependsOn(it.tasks["classes"])
    jar.from(it.java.sourceSets["main"].java.outputDir)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

apply { from("pom.gradle") }

val SourceSet.kotlin: SourceDirectorySet
    get() = withConvention(KotlinSourceSet::class) { kotlin }

import org.apache.maven.model.Dependency
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.2.21"
    id("com.gradle.plugin-publish") version "0.9.10"
}

group = rootProject.group
version = rootProject.version
description = "Run Android application tests on Firebase Test Lab"

repositories {
    jcenter()
    google()
}

dependencies {
    implementation(kotlin("stdlib-jre8", version = "1.2.21"))
    implementation("com.android.tools.build:gradle:3.0.1")
    implementation("com.google.api-client:google-api-client:1.23.0") {
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    implementation("com.google.apis:google-api-services-storage:v1-rev120-1.23.0")
    implementation("com.google.apis:google-api-services-testing:v1-rev28-1.23.0")
    implementation("com.google.apis:google-api-services-toolresults:v1beta3-rev359-1.23.0")

    testRuntimeOnly("com.android.tools.build:gradle:3.0.1")
    testImplementation(kotlin("reflect", version = "1.2.21"))
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

val sourcesJar = task("sourcesJar", Jar::class) {
    from(java.sourceSets["main"].allSource)
}

publishing {
    publications {
        create("sourcesMaven", MavenPublication::class.java) {
            artifacts {
                artifact(sourcesJar) {
                    classifier = "sources"
                }
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val SourceSet.kotlin: SourceDirectorySet
    get() = withConvention(KotlinSourceSet::class) { kotlin }

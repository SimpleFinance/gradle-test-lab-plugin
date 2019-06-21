@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.gradle.PackageOptions

plugins {
    `kotlin-dsl`
    `maven-publish`
    id("kotlinx-serialization") version embeddedKotlinVersion
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.gradle.plugin-publish") version "0.10.1"
    id("org.jmailen.kotlinter") version "1.26.0"
    id("org.jetbrains.dokka") version "0.9.18"
}

val baseVersion: String by rootProject
val snapshot: String by rootProject
val nexusUsername: String by rootProject
val nexusPassword: String by rootProject

val isSnapshot: Boolean get() = snapshot.toBoolean()
val pluginDisplayName = "Gradle plugin for Firebase Test Lab"
val pluginUrl = "https://github.com/SimpleFinance/gradle-test-lab-plugin"

group = "com.simple.gradle.testlab"
version = if (isSnapshot) "$baseVersion-SNAPSHOT" else baseVersion
description = "Run Firebase tests directly from Gradle"

repositories {
    mavenCentral()
    google()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

val shadowed: Configuration by configurations.creating
configurations {
    compileOnly {
        extendsFrom(shadowed)
    }
    testImplementation {
        extendsFrom(shadowed)
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:latest.release")

    shadowed("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.10.0") {
        // Already added to compileOnly by kotlin-dsl
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
    }
    shadowed("com.google.api-client:google-api-client:latest.release")
    shadowed("com.google.apis:google-api-services-storage:latest.release")
    shadowed("com.google.apis:google-api-services-testing:latest.release")
    shadowed("com.google.apis:google-api-services-toolresults:latest.release")
    shadowed("com.google.auth:google-auth-library-oauth2-http:latest.release")

    testImplementation("com.android.tools.build:gradle:3.4+")
    testImplementation("junit:junit:latest.release")
    testImplementation("com.natpryce:hamkrest:latest.release")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shadowed)
        exclude("META-INF/maven/**")
        listOf(
            "com.fasterxml",
            "com.google",
            "io",
            "kotlinx",
            "org.apache",
            "org.checkerframework",
            "org.codehaus"
        ).forEach {
            relocate(it, "com.simple.gradle.testlab.shadow.$it")
        }
    }

    jar {
        enabled = false
        dependsOn(shadowJar)
    }

    pluginUnderTestMetadata {
        pluginClasspath.from.clear()
        pluginClasspath.from(shadowJar)
    }

    test {
        dependsOn(rootProject.tasks.named("customInstallation"))
        dependsOn(shadowJar)
    }

    dokka {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
        jdkVersion = 8
        packageOptions(delegateClosureOf<PackageOptions> {
            prefix = "com.simple.gradle.testlab.internal"
            suppress = true
        })
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

configurations.archives.get().artifacts.clear()
artifacts {
    archives(tasks.shadowJar)
    archives(sourcesJar)
    archives(javadocJar)
}

gradlePlugin {
    plugins {
        register("testLab") {
            id = project.group.toString()
            displayName = pluginDisplayName
            implementationClass = "com.simple.gradle.testlab.TestLabPlugin"
        }
    }
}

pluginBundle {
    website = pluginUrl
    vcsUrl = "$pluginUrl.git"
    description = project.description
    tags = listOf("firebase", "test-lab", "android")
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                pom {
                    name.set(pluginDisplayName)
                    description.set(project.description)
                    url.set(pluginUrl)
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("tad")
                            name.set("Tad Fisher")
                            email.set("tad@simple.com")
                        }
                    }
                    scm {
                        url.set(pluginUrl)
                        connection.set("$pluginUrl.git")
                        tag.set(if (isSnapshot) "master" else "v${project.version}")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "releases"
            url = uri("https://nexus-build.banksimple.com/repository/simple-maven-releases/")
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
        maven {
            name = "snapshots"
            url = uri("https://nexus-build.banksimple.com/repository/simple-maven-snapshots/")
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
}

tasks.withType<PublishToMavenRepository> {
    onlyIf {
        (!isSnapshot && repository.name == "releases") ||
            (isSnapshot && repository.name == "snapshots")
    }
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

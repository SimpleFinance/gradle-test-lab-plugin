@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
    `maven-publish`
    id("kotlinx-serialization") version embeddedKotlinVersion
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.jmailen.kotlinter") version "3.2.0"
    id("org.jetbrains.dokka") version "1.4.10"
}

val baseVersion: String by rootProject
val snapshot: String by rootProject
val githubUser: String? by rootProject
val githubPass: String? by rootProject

val isSnapshot: Boolean get() = snapshot.toBoolean()
val pluginDisplayName = "Gradle plugin for Firebase Test Lab"
val pluginUrl = "https://github.com/SimpleFinance/gradle-test-lab-plugin"

group = "com.simple.gradle.testlab"
version = if (isSnapshot) "$baseVersion-SNAPSHOT" else baseVersion
description = "Run Firebase Test Lab tests directly from Gradle"

repositories {
    mavenCentral()
    google()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

val shadowed: Configuration by configurations.creating {
    // Already added to compileOnly by kotlin-dsl
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    exclude(group = "org.jetbrains", module = "annotations")
}
configurations {
    compileOnly {
        extendsFrom(shadowed)
    }
    testImplementation {
        extendsFrom(shadowed)
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:4.0.+")

    shadowed("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
    shadowed("com.google.api-client:google-api-client:latest.release")
    shadowed("com.google.apis:google-api-services-testing:latest.release")
    shadowed("com.google.apis:google-api-services-toolresults:v1beta3-rev20200513-1.30.9")
    shadowed("com.google.auth:google-auth-library-oauth2-http:latest.release")
    shadowed("com.google.cloud:google-cloud-storage:latest.release")

    testImplementation("com.android.tools.build:gradle:4.0.+")
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
            "google.api",
            "google.cloud",
            "google.geo",
            "google.iam",
            "google.logging",
            "google.longrunning",
            "google.protobuf",
            "google.rpc",
            "google.type",
            "io",
            "kotlinx",
            "org.apache",
            "org.checkerframework",
            "org.codehaus.*",
            "org.threeten.*"
        ).forEach {
            relocate(it, "com.simple.gradle.testlab.shadow.$it")
        }

        doFirst {
            logger.lifecycle(dependsOn.joinToString())
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
        reports {
            junitXml.isEnabled = true
        }
    }

    withType<org.jetbrains.dokka.gradle.DokkaTask> {
        dokkaSourceSets.all {
            jdkVersion.set(8)
            perPackageOption {
                prefix.set("com.simple.gradle.testlab.shadow")
                suppress.set(true)
            }
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
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
            implementationClass = "com.simple.gradle.testlab.TestLabPlugin"
            displayName = pluginDisplayName
            description = project.description
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
        matching { it.name == "pluginMaven" }.withType<MavenPublication> {
            artifact(tasks.shadowJar.get())

            // GitHub Packages appears to be broken when uploading SNAPSHOT JARs with classifiers.
            // https://github.community/t/github-package-registry-as-maven-repo-trouble-uploading-artifact/14226
            if (!isSnapshot) {
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
            }

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
    repositories {
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/simplefinance/gradle-test-lab-plugin")
            credentials {
                username = githubUser
                password = githubPass
            }
        }
    }
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

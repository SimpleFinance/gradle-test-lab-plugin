@file:Suppress("UnstableApiUsage")

import org.jetbrains.dokka.gradle.PackageOptions

plugins {
    `kotlin-dsl`
    `maven-publish`
    kotlin("kapt") version embeddedKotlinVersion
    id("com.github.johnrengelman.shadow") version Versions.com_github_johnrengelman_shadow_gradle_plugin
    id("com.gradle.plugin-publish") version Versions.com_gradle_plugin_publish_gradle_plugin
    id("org.jmailen.kotlinter") version Versions.org_jmailen_kotlinter_gradle_plugin
    id("org.jetbrains.dokka") version Versions.org_jetbrains_dokka_gradle_plugin
}

group = meta.groupId
version = meta.version
description = meta.description

repositories {
    google()
    jcenter()
}

val shadowed by configurations.creating
configurations {
    compileOnly {
        extendsFrom(shadowed)
    }
    testImplementation {
        extendsFrom(shadowed)
    }
}

dependencies {
    compileOnly(Libs.com_android_tools_build_gradle)

    shadowed(Libs.google_api_client) {
        exclude(group = "com.google.guava", module = "guava-jdk5")
    }
    shadowed(Libs.google_api_services_storage)
    shadowed(Libs.google_api_services_testing)
    shadowed(Libs.google_api_services_toolresults)
    shadowed(Libs.moshi)
    kapt(Libs.moshi_kotlin_codegen)

    testImplementation(Libs.com_android_tools_build_gradle)
    testImplementation(Libs.junit)
    testImplementation(Libs.hamkrest)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shadowed)
        exclude("META-INF/maven/**")
        listOf(
            "com.fasterxml",
            "com.google",
            "com.squareup",
            "io",
            "okio",
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
        register("test-lab-plugin") {
            id = meta.pluginId
            displayName = meta.displayName
            implementationClass = "com.simple.gradle.testlab.TestLabPlugin"
        }
    }
}

pluginBundle {
    website = meta.url
    vcsUrl = meta.url
    description = meta.description
    tags = listOf("firebase", "test-lab", "android")
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                groupId = meta.groupId
                artifactId = meta.artifactId
                version = meta.version

                pom {
                    name.set(meta.displayName)
                    description.set(meta.description)
                    url.set(meta.url)
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
                        url.set(meta.url)
                        connection.set(meta.git)
                        tag.set(if (meta.isSnapshot) "master" else "v${meta.version}")
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
                username = meta.nexusUsername
                password = meta.nexusPassword
            }
        }
        maven {
            name = "snapshots"
            url = uri("https://nexus-build.banksimple.com/repository/simple-maven-snapshots/")
            credentials {
                username = meta.nexusUsername
                password = meta.nexusPassword
            }
        }
    }
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

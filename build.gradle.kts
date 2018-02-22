plugins {
    id("com.github.ben-manes.versions") version "0.17.0"
}

group = "com.simple.gradle.testlab"
version = "0.2-SNAPSHOT"

subprojects {
    apply { plugin("maven-publish") }

    group = rootProject.group
    version = rootProject.version

    configure<PublishingExtension> {
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
}

val customInstallationDir = file("$buildDir/custom/gradle-${gradle.gradleVersion}")

val customInstallation by task<Copy> {
    description = "Copies the current Gradle distro into '$customInstallationDir'."

    from(gradle.gradleHomeDir)
    into(customInstallationDir)

    // preserve last modified date on each file to make it easier
    // to check which files were patched by next step
    val copyDetails = mutableListOf<FileCopyDetails>()
    eachFile { copyDetails.add(this) }
    doLast {
        copyDetails.forEach { details ->
            File(customInstallationDir, details.path).setLastModified(details.lastModified)
        }
    }

    // don't bother recreating it
    onlyIf { !customInstallationDir.exists() }
}

tasks.withType<Wrapper> {
    gradleVersion = "4.5.1"
    distributionType = Wrapper.DistributionType.ALL
}

inline fun <reified T : Task> task(noinline configuration: T.() -> Unit) = tasks.creating(T::class, configuration)

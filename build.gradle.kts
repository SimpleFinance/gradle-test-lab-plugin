plugins {
    base
}

val customInstallationDir = file("$buildDir/custom/gradle-${gradle.gradleVersion}")

val customInstallation by tasks.registering(Copy::class) {
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

subprojects {
    buildscript {
        dependencyLocking.lockAllConfigurations()
    }
    dependencyLocking.lockAllConfigurations()
}

tasks.withType<Wrapper> {
    gradleVersion = "6.5"
    distributionType = Wrapper.DistributionType.ALL
}

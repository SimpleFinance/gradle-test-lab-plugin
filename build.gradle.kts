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

allprojects {
    buildscript {
        dependencyLocking.lockAllConfigurations()
    }
    dependencyLocking.lockAllConfigurations()
}

tasks.withType<Wrapper> {
    gradleVersion = "5.4.1"
    distributionType = Wrapper.DistributionType.ALL
}

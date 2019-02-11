plugins {
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    base
}

group = "com.simple.gradle.testlab"
version = "0.3-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
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

tasks.withType<Wrapper> {
    gradleVersion = Versions.Gradle.currentVersion
    distributionType = Wrapper.DistributionType.ALL
}

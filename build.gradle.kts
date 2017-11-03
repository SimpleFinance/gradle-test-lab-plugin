plugins {
    `maven-publish`
}

group = "com.simple.gradle.testlab"
version = "0.1-SNAPSHOT"

subprojects {
    apply { plugin("maven-publish") }

    group = rootProject.group
    version = rootProject.version

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "releases"
                url = uri("https://nexus-build.banksimple.com/repository/simple-maven-releases/")
            }
            maven {
                name = "snapshots"
                url = uri("https://nexus-build.banksimple.com/repository/simple-maven-snapshots/")
            }
        }
    }
}

task("wrapper", type = Wrapper::class) {
    group = "build setup"
    gradleVersion = "4.3"
    distributionType = Wrapper.DistributionType.ALL
}

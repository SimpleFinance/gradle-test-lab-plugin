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

tasks.withType<Wrapper> {
    gradleVersion = "4.5.1"
    distributionType = Wrapper.DistributionType.ALL
}

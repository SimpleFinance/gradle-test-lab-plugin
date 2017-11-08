plugins {
    `maven-publish`
      id("com.github.ben-manes.versions") version "0.17.0"
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
            // TODO Remove these
            maven {
                name = "oldReleases"
                url = uri("http://nexus.banksimple.com/content/repositories/releases/")
            }
            maven {
                name = "oldSnapshots"
                url = uri("http://nexus.banksimple.com/content/repositories/snapshots/")
            }
            withType<MavenArtifactRepository> {
                credentials {
                    username = properties["nexusUsername"]?.toString()
                    password = properties["nexusPassword"]?.toString()
                }
            }
        }
    }
}

task("wrapper", type = Wrapper::class) {
    group = "build setup"
    gradleVersion = "4.3"
    distributionType = Wrapper.DistributionType.ALL
}

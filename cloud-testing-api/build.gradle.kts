plugins {
    `maven-publish`
    `java-library`
}

group = rootProject.group
version = rootProject.version

configurations {
    "virtualenv"()
}

repositories {
    mavenCentral()
    ivy {
        name = "pypi"
        url = uri("https://files.pythonhosted.org/packages/source/v/")
        layout("pattern", closureOf<IvyPatternRepositoryLayout> {
            artifact("[organization]/[module]-[revision].[ext]")
        })
    }
}

dependencies {
    compileOnly("com.google.api-client:google-api-client:1.23.0")
    "virtualenv"("virtualenv:virtualenv:15.1.0@tar.gz")
}

publishing {
    publications {
        create("cloudTestingApi", MavenPublication::class.java) {
            artifact(tasks["jar"])
        }
    }
}

val venv = file("$buildDir/virtualenv")
val genApiEnv = file("$buildDir/gen-api-env")
val genApiBin = file("$genApiEnv/bin/generate_library")

task("installVirtualenv", type = Copy::class) {
    from(tarTree(configurations["virtualenv"].singleFile)) {
        eachFile {
            relativePath = RelativePath.parse(
                    relativePath.isFile,
                    relativePath.segments.drop(1).joinToString("/"))
        }
    }
    into(venv)
}

task("generateApiEnv", type = Exec::class) {
    dependsOn(tasks["installVirtualenv"])
    outputs.dir(genApiEnv)
    commandLine("python2", "$venv/virtualenv.py", "$genApiEnv")
}

task("installPackages", type = Exec::class) {
    dependsOn("generateApiEnv")
    inputs.file("requirements.txt")
    outputs.file(genApiBin)
    commandLine("$genApiEnv/bin/python", "-m", "pip", "install", "-r", "requirements.txt")
}

task("generateTestingApi", type = Exec::class) {
    dependsOn("installPackages")
    group = "build"
    inputs.file(file("$projectDir/src/testing_v1.json"))
    outputs.dir("$buildDir/generated/source/testing-api")

    commandLine("$genApiEnv/bin/python", "$genApiBin",
            "--input=$projectDir/src/testing_v1.json",
            "--language=java",
            "--output_dir=$buildDir/generated/source/testing-api")
}

tasks {
    "compileJava" {
        dependsOn("generateTestingApi")
    }
}

java {
    sourceSets {
        "main" {
            java.srcDir("$buildDir/generated/source/testing-api")
        }
    }
}

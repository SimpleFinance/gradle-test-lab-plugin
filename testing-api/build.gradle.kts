plugins {
    `java-library`
    id("com.jetbrains.python.envs") version "0.0.19"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.api-client:google-api-client:1.23.0")
}

envs {
    bootstrapDirectory = File(buildDir, "bootstrap")
    envsDirectory = File(buildDir, "envs")
    condaenv("python27", "2.7", listOf("google-apis-client-generator==1.4.3"))
}

task("generateTestingApi", type = Exec::class) {
    group = "build"
    inputs.file(file("$projectDir/src/testing_v1.json"))
    outputs.dir("$buildDir/generated/source/testing-api")

    val env = envs.condaEnvs[0]
    commandLine = listOf("${env.envDir}/bin/generate_library",
            "--input=$projectDir/src/testing_v1.json",
            "--language=java",
            "--output_dir=$buildDir/generated/source/testing-api")
}

java {
    sourceSets {
        "main" {
            java.srcDir("$buildDir/generated/source/testing-api")
        }
    }
}

afterEvaluate {
    tasks {
        "generateTestingApi" {
            dependsOn(tasks["build_envs"])
        }
        "compileJava" {
            dependsOn(tasks["generateTestingApi"])
        }
    }
}

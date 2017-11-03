package com.simple.gradle.testlab.model

import org.gradle.api.Project
import java.io.File

class Apk(project: Project) {
    val path = project.objects.property(File::class.java)
    val packageId = project.objects.property(String::class.java)
}

class Apks(project: Project) {
    val app = project.objects.property(Apk::class.java)
    val test = project.objects.property(Apk::class.java)
}
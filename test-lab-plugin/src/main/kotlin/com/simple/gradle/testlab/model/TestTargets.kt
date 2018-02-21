package com.simple.gradle.testlab.model

interface TestTargets {
    val targets: MutableList<String>

    fun addPackage(packageName: String): Boolean
    fun addClass(className: String): Boolean
    fun addMethod(className: String, methodName: String): Boolean
}

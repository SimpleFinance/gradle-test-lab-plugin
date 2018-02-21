package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.TestTargets

class DefaultTestTargets : TestTargets {
    override val targets = mutableListOf<String>()

    override fun addPackage(packageName: String) = targets.add("package $packageName")
    override fun addClass(className: String) = targets.add("class $className")
    override fun addMethod(className: String, methodName: String) = targets.add("class $className#$methodName")
}

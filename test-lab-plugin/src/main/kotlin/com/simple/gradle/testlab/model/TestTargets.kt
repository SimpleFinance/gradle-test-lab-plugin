package com.simple.gradle.testlab.model

/** The list of test targets to execute during the instrumentation test. */
interface TestTargets {
    /**
     * A list of test targets to execute during the instrumentation test.
     *
     * @see addPackage
     * @see addClass
     * @see addMethod
     */
    val targets: MutableList<String>

    /**
     * Add a package target.
     *
     * @param packageName the fully-qualified package name
     */
    fun addPackage(packageName: String): Boolean

    /**
     * Add a class target.
     *
     * @param className the fully-qualified class name
     */
    fun addClass(className: String): Boolean

    /**
     * Add a method target.
     *
     * @param className the fully-qualified class name
     * @param methodName the method to execute
     */
    fun addMethod(className: String, methodName: String): Boolean
}

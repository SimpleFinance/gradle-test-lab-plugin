package com.simple.gradle.testlab.model

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface TestTargets {
    /**
     * Includes all tests in the given packages. Package names prefixed with `!` will be excluded.
     *
     *     com.my.package.included
     *     !com.my.package.excluded
     */
    @get:[Input Optional] val packages: SetProperty<String>

    /**
     * Includes all tests in the given classes or test methods. Class names prefixed with `!` will be excluded.
     *
     * Classes are fully qualified, such as:
     *
     *     com.my.package.MyClass
     *     !com.my.package.OtherClass
     *
     * Individual test methods can also be specified:
     *
     *     com.my.package.MyClass#myMethod
     *     !com.my.package.OtherClass#otherMethod
     */
    @get:[Input Optional] val classes: SetProperty<String>

    /**
     * Exclude tests annotated with *all* of the given annotaitons.. Annotations are fully-qualified. Annotation names
     * prefixed with `!` are excluded.
     *
     *     com.my.package.MyAnnotation
     *     !com.my.package.OtherAnnotation
     */
    @get:[Input Optional] val annotations: SetProperty<String>

    /**
     * Include tests listed in a file located at the given path on the target device. See
     * [https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#execution-options], section
     * "Running all tests listed in a file" for the syntax of test files.
     *
     * @see InstrumentationTest.files
     */
    @get:[Input Optional] val includeFile: Property<String>

    /**
     * Exclude tests listed in a file located at the given path on the target device. See
     * [https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner#execution-options], section
     * "Running all tests listed in a file" for the syntax of test files.
     */
    @get:[Input Optional] val excludeFile: Property<String>

    /**
     * Adds tests matching the given regular expression.
     */
    @get:[Input Optional] val regex: Property<String>

    /**
     * Adds tests annotated with `SmallTest`, `MediumTest`, or `LargeTest` annotations.
     */
    @get:[Input Optional] val size: Property<TestSize>
}

enum class TestSize(internal val argument: String) {
    /**
     * Corresponds to tests annotated with `androidx.test.filters.SmallTest`.
     */
    SMALL("small"),
    /**
     * Corresponds to tests annotated with `androidx.test.filters.MediumTest`.
     */
    MEDIUM("medium"),
    /**
     * Corresponds to tests annotated with `androidx.test.filters.LargeTest`.
     */
    LARGE("large")
}

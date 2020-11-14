package com.simple.gradle.testlab

import com.simple.gradle.testlab.internal.DefaultTestTargets.Companion.format
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder

class DefaultTestTargetsTest {

    @Test
    fun formatsClasses() {
        val targets = setOf(
            "com.foo.Bar",
            "com.foo.Bar#Baz",
            "!com.foo.Quux",
            "!com.foo.Quux#Bat"
        )

        expectThat(targets.format("class")).containsExactlyInAnyOrder(
            "class com.foo.Bar,com.foo.Bar#Baz",
            "notClass com.foo.Quux,com.foo.Quux#Bat"
        )
    }
}

package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.RoboDirectives

class DefaultRoboDirectives : RoboDirectives {
    override val directives = mutableListOf<DefaultRoboDirective>()

    override fun click(resourceName: String) {
        directives.add(DefaultRoboDirective("click", resourceName))
    }

    override fun text(resourceName: String, inputText: String) {
        directives.add(DefaultRoboDirective("text", resourceName, inputText))
    }
}

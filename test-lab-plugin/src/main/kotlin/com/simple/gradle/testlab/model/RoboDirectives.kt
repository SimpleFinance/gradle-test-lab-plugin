package com.simple.gradle.testlab.model

interface RoboDirectives {
    val directives: MutableList<out RoboDirective>

    fun click(resourceName: String)
    fun text(resourceName: String, inputText: String)
}

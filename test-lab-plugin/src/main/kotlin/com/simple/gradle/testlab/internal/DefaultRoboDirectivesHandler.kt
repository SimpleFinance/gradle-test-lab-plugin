package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.RoboDirective
import com.simple.gradle.testlab.model.RoboDirectivesHandler
import org.gradle.api.provider.ListProperty

internal class DefaultRoboDirectivesHandler(
    private val directives: ListProperty<RoboDirective>
) : RoboDirectivesHandler {
    override fun click(resourceName: String) =
        directives.add(DefaultRoboDirective("click", resourceName))

    override fun text(resourceName: String, inputText: String) =
        directives.add(DefaultRoboDirective("text", resourceName, inputText))
}

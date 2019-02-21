package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.RoboDirective

internal data class DefaultRoboDirective(
    override var actionType: String,
    override var resourceName: String,
    override var inputText: String? = null
) : RoboDirective

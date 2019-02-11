package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.Orientation

@Suppress("UnstableApiUsage")
internal data class DefaultDevice(
    override val model: String,
    override val api: Int,
    override val locale: String,
    override val orientation: Orientation
) : Device

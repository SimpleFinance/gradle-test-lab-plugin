package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.Orientation

internal class DefaultDevice : Device {
    override var modelId: String = "hammerhead"
    override var version: Int = 21
    override var locale: String = "en"
    override var orientation: Orientation = Orientation.PORTRAIT
}

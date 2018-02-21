package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.Device
import com.simple.gradle.testlab.model.Orientation
import groovy.lang.Closure
import org.gradle.util.Configurable
import org.gradle.util.ConfigureUtil

class DefaultDevice : Device, Configurable<Device> {
    override var modelId: String = "hammerhead"
    override var version: Int = 21
    override var locale: String = "en"
    override var orientation: Orientation = Orientation.PORTRAIT

    override fun configure(configureClosure: Closure<*>): Device {
        ConfigureUtil.configureSelf(configureClosure, this)
        return this
    }
}

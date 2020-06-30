package com.simple.gradle.testlab.model

import com.simple.gradle.testlab.internal.DefaultDevice
import org.gradle.api.tasks.Input

@Suppress("UnstableApiUsage")
interface Device {
    /** The model ID of the Android device to be used. */
    @get:Input val model: String

    /** The ID of the Android OS version to be used. */
    @get:Input val api: Int

    /** The local of the test device used for testing. */
    @get:Input val locale: String

    /** How the device is oriented during the test. */
    @get:Input val orientation: Orientation

    companion object {
        val DEFAULT: Device = DefaultDevice("hammerhead", 21, "en", Orientation.PORTRAIT)
    }

    class Builder {
        var model: String = DEFAULT.model
        var api: Int = DEFAULT.api
        var locale: String = DEFAULT.locale
        var orientation: Orientation = DEFAULT.orientation

        internal fun build(): Device = DefaultDevice(model, api, locale, orientation)
    }
}

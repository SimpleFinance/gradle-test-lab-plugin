package com.simple.gradle.testlab.model

interface Device {
    /** The id of the Android device to be used. */
    var modelId: String

    /** The id of the Android OS version to be used. */
    var version: Int

    /** The local of the test device used for testing. */
    var locale: String

    /** How the device is oriented during the test. */
    var orientation: Orientation
}

package com.simple.gradle.testlab.model

interface Device {
    var modelId: String
    var version: Int
    var locale: String
    var orientation: Orientation
}

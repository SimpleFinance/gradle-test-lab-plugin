package com.simple.gradle.testlab.model

class Device {
    var model: String = "hammerhead"
    var version: Int = 21
    var locale: String = "en"
    var orientation: Orientation = Orientation.PORTRAIT
}

enum class Orientation { LANDSCAPE, PORTRAIT }

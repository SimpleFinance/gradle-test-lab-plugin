package com.simple.gradle.testlab.model

class Matrix {
    var locales: List<String> = listOf("en")
    var orientations: List<Orientation> = listOf(Orientation.PORTRAIT)
    var androidApiLevels: List<Int> = emptyList()
    var deviceIds: List<String> = emptyList()
    var timeoutSec: Long = 0
}


package com.simple.gradle.testlab.internal

import java.io.File

internal sealed class DeviceFile {

    enum class Type { OBB, REGULAR }

    internal data class Obb(val source: File, val filename: String) : DeviceFile()

    internal data class Regular(val source: File, val devicePath: String) : DeviceFile()
}
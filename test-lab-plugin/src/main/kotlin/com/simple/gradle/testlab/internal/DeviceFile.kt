package com.simple.gradle.testlab.internal

import org.gradle.api.tasks.Input
import java.io.File

sealed class DeviceFile {

    enum class Type { OBB, REGULAR }

    internal data class Obb(
        @Input val source: File,
        @Input val filename: String
    ) : DeviceFile()

    internal data class Regular(
        @Input val source: File,
        @Input val devicePath: String
    ) : DeviceFile()
}

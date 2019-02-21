package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.DeviceFilesHandler
import org.gradle.api.provider.ListProperty
import java.io.File

@Suppress("UnstableApiUsage")
internal class DefaultDeviceFilesHandler(
    private val files: ListProperty<DeviceFile>
) : DeviceFilesHandler {
    override fun obb(source: File, filename: String) {
        files.add(DeviceFile.Obb(source, filename))
    }

    override fun push(source: File, devicePath: String) {
        files.add(DeviceFile.Regular(source, devicePath))
    }
}
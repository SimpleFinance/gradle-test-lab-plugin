package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.DeviceFile
import com.google.api.services.testing.model.FileReference
import com.google.api.services.testing.model.ObbFile
import com.google.api.services.testing.model.RegularFile
import com.simple.gradle.testlab.internal.DeviceFile.Type

internal data class DeviceFileReference(
    val type: Type,
    val file: FileReference,
    val dest: String
) {
    val asDeviceFile: DeviceFile get() = DeviceFile().apply {
        when (type) {
            Type.OBB -> obbFile = ObbFile().setObb(file).setObbFileName(dest)
            Type.REGULAR -> regularFile = RegularFile().setContent(file).setDevicePath(dest)
        }
    }
}

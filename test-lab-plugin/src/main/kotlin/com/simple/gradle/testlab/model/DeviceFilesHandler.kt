package com.simple.gradle.testlab.model

import java.io.File

interface DeviceFilesHandler {
    /**
     * Opaque Binary Blob (OBB) file(s) to install on the device.
     *
     * @param source path to the source OBB file
     * @param filename OBB file name which must conform to the format as specified by Android, e.g.
     *     `[main|patch].0300110.com.example.android.obb`, which will be installed into
     *     `/Android/obb/` on the device.
     */
    fun obb(source: File, filename: String)

    /**
     * A file or directory to install on the device before the test starts.
     *
     * @param source path to the source file
     * @param devicePath Where to put the content on the device. Must be an absolute, whitelisted
     *     path. If the file exists, it will be replaced. The following device-side directories
     *     and any of their subdirectories are whitelisted:
     *
     *     - `${EXTERNAL_STORAGE}`
     *     - `/sdcard/${ANDROID_DATA}/local/tmp`
     *     - `/data/local/tmp`
     *
     *     Specifying a path outside of these directory trees is invalid.
     */
    fun push(source: File, devicePath: String)
}

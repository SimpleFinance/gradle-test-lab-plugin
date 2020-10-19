package com.simple.gradle.testlab.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class UploadResults(
    val appApk: String,
    val testApk: String?,
    val additionalApks: List<String>,
    val deviceFiles: List<UploadedFile>
) {
    companion object {
        fun fromJson(text: String): UploadResults = DefaultJson.parse(serializer(), text)
    }
}

internal fun UploadResults.toJson(): String =
    DefaultJson.stringify(UploadResults.serializer(), this)

@Serializable
internal data class UploadedFile(
    val type: DeviceFile.Type,
    val path: String,
    val dest: String
)

internal val UploadedFile.asDeviceFileReference: DeviceFileReference
    get() = DeviceFileReference(type, path.asFileReference, dest)

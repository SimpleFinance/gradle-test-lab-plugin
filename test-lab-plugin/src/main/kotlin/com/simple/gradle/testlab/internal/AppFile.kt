package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.FileReference
import com.simple.gradle.testlab.model.FileType
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.list

@Serializable
internal data class AppFile(
    val type: FileType,
    @Serializable(with = FileReferenceSerializer::class) val path: FileReference
) {
    companion object {
        fun fromJson(text: String): List<AppFile> = DefaultJson.parse(serializer().list, text)
    }
}

internal fun List<AppFile>.toJson(): String = DefaultJson.stringify(AppFile.serializer().list, this)

@Serializer(forClass = FileReference::class)
object FileReferenceSerializer : KSerializer<FileReference> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("com.simple.gradle.testlab.internal.FileReferenceSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): FileReference =
        FileReference().setGcsPath(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: FileReference) {
        encoder.encodeString(value.gcsPath)
    }
}

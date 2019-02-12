package com.simple.gradle.testlab.internal

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import java.io.File

@JsonClass(generateAdapter = true)
internal data class UploadResults(
    val appApk: String,
    val testApk: String?,
    val additionalApks: List<String>
) {
    companion object {
        private val jsonAdapter = Moshi.Builder().build().adapter(UploadResults::class.java)

        fun readFrom(file: File): UploadResults = jsonAdapter.fromJson(file.readText())!!
    }

    fun writeTo(file: File) = file.writeText(jsonAdapter.toJson(this))
}

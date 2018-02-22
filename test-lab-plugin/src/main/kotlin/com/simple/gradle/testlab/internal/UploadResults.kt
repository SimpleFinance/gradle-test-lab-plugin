package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.FileReference
import java.io.File

internal class UploadResults {
    val references = mutableMapOf<File, FileReference>()
}

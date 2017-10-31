package com.simple.gradle.testlab

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TestLabExtensionTest {
    @get:Rule val projectDir = TemporaryFolder()

    lateinit var buildFile: File

    @Before
    fun setup() {
        buildFile = projectDir.newFile("build.gradle")
        buildFile.appendText("""
            plugins { id 'com.simple.gradle.testlab' }
            """)
    }

    @Test
    fun `dsl`() {
        buildFile.appendText("""
            testLab {

            }
            """)
    }
}
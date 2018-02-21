package com.simple.gradle.testlab

import com.simple.gradle.testlab.internal.DefaultGoogleApi
import com.simple.gradle.testlab.model.GoogleApi
import com.simple.gradle.testlab.model.TestConfigContainer
import org.gradle.api.Action
import org.gradle.api.tasks.Internal
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Random

open class TestLabExtension(
    val tests: TestConfigContainer
) {
    val googleApi: DefaultGoogleApi = DefaultGoogleApi()

    @delegate:Internal internal val prefix by lazy { getUniquePathPrefix() }

    fun googleApi(configure: Action<in GoogleApi>) {
        configure.execute(googleApi)
    }

    fun tests(configure: Action<in TestConfigContainer>) {
        configure.execute(tests)
    }

    private fun getUniquePathPrefix(): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val suffixLength = 4
        val randomGenerator = Random()

        val suffix = StringBuilder(suffixLength)
        for (i in 0 until suffixLength) {
            suffix.append(characters[randomGenerator.nextInt(characters.length)])
        }

        val creationTime = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME)
        return "gradle-build_" + creationTime.replace(' ', '_').replace(',', '.') + "_" + suffix
    }
}

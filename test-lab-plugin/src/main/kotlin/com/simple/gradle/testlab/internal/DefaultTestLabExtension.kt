package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApi
import com.simple.gradle.testlab.model.TestConfigContainer
import groovy.lang.Closure
import org.gradle.util.ConfigureUtil
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Random

internal class DefaultTestLabExtension(
    override val tests: TestConfigContainer
) : TestLabExtensionInternal {
    override val googleApi: DefaultGoogleApi = DefaultGoogleApi()

    override val prefix by lazy { getUniquePathPrefix() }

    override fun googleApi(configure: Closure<*>): GoogleApi =
        googleApi.apply { ConfigureUtil.configure(configure, this) }

    override fun googleApi(configure: GoogleApi.() -> Unit): GoogleApi =
        googleApi.apply(configure)

    override fun tests(configure: Closure<*>): TestConfigContainer =
        tests.apply { ConfigureUtil.configure(configure, this) }

    override fun tests(configure: TestConfigContainer.() -> Unit): TestConfigContainer =
        tests.apply(configure)

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

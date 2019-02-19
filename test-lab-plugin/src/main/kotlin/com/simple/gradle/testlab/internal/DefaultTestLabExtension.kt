package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.GoogleApiConfig
import com.simple.gradle.testlab.model.TestConfig
import com.simple.gradle.testlab.model.TestConfigHandler
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Random
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultTestLabExtension @Inject constructor(
    private val objects: ObjectFactory
) : TestLabExtensionInternal {
    override val googleApi: DefaultGoogleApiConfig = DefaultGoogleApiConfig()
    override val tests = objects.mapProperty<String, TestConfig>()
    override val prefix by lazy { getUniquePathPrefix() }

    override val testsInternal by lazy {
        tests.map { tests -> tests.values.map { it as TestConfigInternal } }
    }

    private val testConfigHandler by lazy {
        objects.newInstance<DefaultTestConfigHandler>(tests)
    }

    override fun googleApi(configure: Action<GoogleApiConfig>) {
        googleApi.apply(configure::execute)
    }

    override fun tests(configure: Action<TestConfigHandler>) =
        configure.execute(testConfigHandler)

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

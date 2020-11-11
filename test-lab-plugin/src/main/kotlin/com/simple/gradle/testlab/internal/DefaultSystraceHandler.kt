package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.SystraceHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultSystraceHandler @Inject constructor(
    objects: ObjectFactory
) : SystraceHandler {
    override val enabled = objects.property<Boolean>().value(false)
    override val durationSeconds = objects.property<Int>()
}

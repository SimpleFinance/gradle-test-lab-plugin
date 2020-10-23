package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.FileReference
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.newInstance
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// Logging helper

internal val Any.log: Logger
    get() = getLoggerForClass()(javaClass)

private fun getLoggerForClass(): (Class<*>) -> Logger = { c: Class<*> -> Logging.getLogger(c) }.memoize()

private const val DEFAULT_CAPACITY = 1 shl 8
internal fun <A, R> ((A) -> R).memoize(initialCapacity: Int = DEFAULT_CAPACITY): (A) -> R {
    val cache: MutableMap<A, R> = HashMap(initialCapacity)
    return { a: A ->
        cache.getOrPut(a) { this(a) }
    }
}

// Extensions

internal operator fun <T> SetProperty<T>.invoke(value: T) = SetPropertyDelegate(this, value)

internal class SetPropertyDelegate<T>(
    private val collection: SetProperty<T>,
    private val value: T
) : ReadWriteProperty<Any?, Boolean> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        collection.orNull?.contains(value) == true

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        if (value) collection.add(this.value) else collection.orNull?.remove(this.value)
    }
}

internal val String.asFileReference: FileReference
    get() = FileReference().setGcsPath(this)

@Suppress("UnstableApiUsage")
internal fun <T : Named, C : ExtensiblePolymorphicDomainObjectContainer<T>>
ObjectFactory.customPolymorphicContainer(containerType: KClass<C>): C = newInstance(containerType)

internal val DefaultJson = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

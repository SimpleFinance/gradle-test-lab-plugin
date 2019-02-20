package com.simple.gradle.testlab.internal

import com.google.api.services.storage.Storage
import com.google.api.services.storage.model.StorageObject
import com.google.api.services.testing.model.FileReference
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// Logging helper

internal val Any.log: Logger
    get() = getLoggerForClass()(javaClass)

private fun getLoggerForClass(): (Class<*>) -> Logger =
    { c: Class<*> -> Logging.getLogger(c) }.memoize()

private const val DEFAULT_CAPACITY = 1 shl 8
internal fun <A, R> ((A) -> R).memoize(initialCapacity: Int = DEFAULT_CAPACITY): (A) -> R {
    val cache: MutableMap<A, R> = HashMap(initialCapacity)
    return { a: A ->
        cache.getOrPut(a) { this(a) }
    }
}

// StorageObject list iterator

internal fun Storage.Objects.List.all(): Iterable<StorageObject> = StorageObjectsIterable(this).flatten()

private class StorageObjectsIterable(
    private val request: Storage.Objects.List
) : Iterable<Iterable<StorageObject>> {
    override fun iterator(): Iterator<Iterable<StorageObject>> = StorageListIterator(request)
}

private class StorageListIterator(
    private val request: Storage.Objects.List
) : AbstractIterator<Iterable<StorageObject>>() {
    private var nextPageToken: String? = null
    private var hasFetched: Boolean = false

    override fun computeNext() {
        if (nextPageToken == null && hasFetched) {
            done()
            return
        }
        val result = request.setPageToken(nextPageToken).execute()
        if (result?.items != null) {
            setNext(result.items)
        } else {
            done()
        }
        nextPageToken = result?.nextPageToken
        hasFetched = true
    }
}

// Extensions

internal operator fun <T> MutableSet<T>.invoke(value: T) = SetPropertyDelegate(this, value)

internal class SetPropertyDelegate<T>(
    private val collection: MutableSet<T>,
    private val value: T
) : ReadWriteProperty<Any?, Boolean> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        collection.contains(value)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        if (value) collection.add(this.value) else collection.remove(this.value)
    }
}

internal val String.asFileReference: FileReference
    get() = FileReference().setGcsPath(this)

@Suppress("UnstableApiUsage")
internal fun <T : Named, C : ExtensiblePolymorphicDomainObjectContainer<T>>
    ObjectFactory.customPolymorphicContainer(containerType: KClass<C>): C = newInstance(containerType)
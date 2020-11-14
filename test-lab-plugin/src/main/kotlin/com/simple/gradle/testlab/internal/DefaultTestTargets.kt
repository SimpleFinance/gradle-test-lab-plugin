package com.simple.gradle.testlab.internal

import com.google.common.annotations.VisibleForTesting
import com.simple.gradle.testlab.model.TestSize
import com.simple.gradle.testlab.model.TestTargets
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultTestTargets @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : TestTargets {
    override val packages: SetProperty<String> = objects.setProperty<String>().empty()
    override val classes: SetProperty<String> = objects.setProperty<String>().empty()
    override val annotations: SetProperty<String> = objects.setProperty<String>().empty()
    override val includeFile: Property<String> = objects.property()
    override val excludeFile: Property<String> = objects.property()
    override val regex: Property<String> = objects.property()
    override val size: Property<TestSize> = objects.property()

    @get:Internal
    val targets: Provider<List<String>> = providers.provider {
        packages.finalizeValue()
        classes.finalizeValue()
        annotations.finalizeValue()
        includeFile.finalizeValue()
        excludeFile.finalizeValue()
        regex.finalizeValue()
        size.finalizeValue()

        listOfNotNull(
            includeFile.orNull?.let { "testFile $it" },
            excludeFile.orNull?.let { "notTestFile $it" },
            regex.orNull?.let { "tests_regex $it" },
            size.orNull?.let { "size ${it.argument}" }
        ) + packages.get().format("package") +
            classes.get().format("class") +
            annotations.get().format("annotation")
    }

    companion object {
        @VisibleForTesting
        internal fun Set<String>.format(prefix: String): List<String> {
            val (excludes, includes) = filterNot(String::isBlank)
                .takeUnless(List<String>::isEmpty)
                ?.map(String::trim)
                ?.partition { it.startsWith("!") }
                ?: return emptyList()
            return listOfNotNull(
                excludes
                    .takeUnless(List<String>::isEmpty)
                    ?.joinToString(",", "not${prefix.capitalize()} ") { it.removePrefix("!") },
                includes
                    .takeUnless(List<String>::isEmpty)
                    ?.joinToString(",", "$prefix ")
            )
        }
    }
}

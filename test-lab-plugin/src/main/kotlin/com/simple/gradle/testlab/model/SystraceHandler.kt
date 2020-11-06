package com.simple.gradle.testlab.model

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Configure systrace collection.
 */
interface SystraceHandler {
    /**
     * `true` to enable systrace collection for this test.
     */
    @get:Input val enabled: Property<Boolean>

    /**
     * Systrace duration in seconds. Should be between 1 and 30 seconds.
     */
    @get:[Input Optional] val durationSeconds: Property<Int>
}

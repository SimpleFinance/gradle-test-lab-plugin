package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface TestTargetsHandler : TestTargets {

    /**
     * Uniformly distribute test targets among this number of shards.
     *
     * If the test matrix contains physical devices, the limit is 50; otherwise, the limit is 500 shards.
     */
    @get:Input
    val shardCount: Property<Int>

    /**
     * Groups of test targets to execute in parallel across all devices.
     *
     * If the test matrix contains physical devices, the limit is 50; otherwise, the limit is 500 shards.
     */
    @get:Input
    val shards: ListProperty<out TestTargets>

    /**
     * Add an explicit shard of test targets.
     *
     * Shards are executed in parallel across all devices.
     *
     * If the test matrix contains physical devices, the limit is 50; otherwise, the limit is 500 shards.
     */
    fun shard(configure: Action<in TestTargets>)
}

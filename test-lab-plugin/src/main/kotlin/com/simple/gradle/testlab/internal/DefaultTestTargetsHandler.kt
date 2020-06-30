package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.TestTargets
import com.simple.gradle.testlab.model.TestTargetsHandler
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal open class DefaultTestTargetsHandler @Inject constructor(
    private val objects: ObjectFactory,
    providers: ProviderFactory
) : DefaultTestTargets(objects, providers), TestTargetsHandler {

    override val shardCount = objects.property<Int>().value(1)

    override val shards = objects.listProperty<DefaultTestTargets>()

    override fun shard(configure: Action<in TestTargets>) {
        val shard = objects.newInstance<DefaultTestTargets>()
        configure.execute(shard)
        shards.add(shard)
    }
}

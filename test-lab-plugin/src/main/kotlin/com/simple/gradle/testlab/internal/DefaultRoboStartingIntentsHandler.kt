package com.simple.gradle.testlab.internal

import com.simple.gradle.testlab.model.IntentTimeoutHandler
import com.simple.gradle.testlab.model.RoboStartingIntentsHandler
import com.simple.gradle.testlab.model.StartActivityIntentHandler
import com.simple.gradle.testlab.model.StartingIntent
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

internal open class DefaultRoboStartingIntentsHandler @Inject constructor(
    private val objects: ObjectFactory,
    private val intents: ListProperty<StartingIntent>
) : RoboStartingIntentsHandler {
    override fun launcherActivity() {
        intents.add(StartingIntent.LauncherActivity(timeout = null))
    }

    override fun launcherActivity(configure: Action<in IntentTimeoutHandler>) {
        val handler = objects.newInstance<DefaultLauncherActivityIntentHandler>()
        configure.execute(handler)
        intents.add(handler.startingIntent)
    }

    override fun startActivity(configure: Action<in StartActivityIntentHandler>) {
        val handler = objects.newInstance<DefaultStartActivityIntentHandler>()
        configure.execute(handler)
        intents.add(handler.startingIntent)
    }
}

internal open class DefaultLauncherActivityIntentHandler @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : IntentTimeoutHandler {
    override val timeout = objects.property<Int>()

    val startingIntent = providers.provider {
        StartingIntent.LauncherActivity(
            timeout.get().toString()
        )
    }
}

internal open class DefaultStartActivityIntentHandler @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
) : StartActivityIntentHandler {
    override val action = objects.property<String>()
    override val categories = objects.listProperty<String>()
    override val uri = objects.property<String>()
    override val timeout = objects.property<Int>()

    val startingIntent = providers.provider {
        StartingIntent.StartActivity(
            action.get(),
            categories.get(),
            uri.get(),
            timeout.get().toString()
        )
    }
}

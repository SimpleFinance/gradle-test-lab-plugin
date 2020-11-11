package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Specify the start activities to crawl.
 */
interface RoboStartingIntentsHandler {
    /** Add an intent that starts the main launcher activity. */
    fun launcherActivity()

    /** Add an intent that starts the main launcher activity. */
    fun launcherActivity(configure: Action<in IntentTimeoutHandler>)

    /** Add an intent that starts an activity with specific details. */
    fun startActivity(configure: Action<in StartActivityIntentHandler>)
}

interface IntentTimeoutHandler {
    /** Timeout in seconds for each intent. */
    @get:[Input Optional] val timeout: Property<Int>
}

/**
 * A starting intent specified by an action, uri, and categories.
 */
interface StartActivityIntentHandler : IntentTimeoutHandler {
    /** Action name. Required for START_ACTIVITY. */
    @get:Input val action: Property<String>

    /** Intent categories to set on the intent. */
    @get:[Input Optional] val categories: ListProperty<String>

    /** URI for the action. */
    @get:[Input Optional] val uri: Property<String>
}

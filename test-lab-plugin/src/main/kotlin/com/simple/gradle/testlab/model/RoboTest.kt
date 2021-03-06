package com.simple.gradle.testlab.model

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

/**
 * A test of an android application that explores the application on a virtual or physical Android
 * device, finding culprits and crashes as it goes.
 */
@Suppress("UnstableApiUsage")
interface RoboTest : TestConfig {

    /**
     * A set of directives Robo should apply during the crawl. This allows users to customize the
     * crawl. For example, the username and password for a test account can be provided.
     * Optional.
     */
    @get:[Nested Optional] val directives: ListProperty<RoboDirective>

    /** A JSON file with a sequence of actions Robo should perform as a prologue for the crawl. Optional. */
    @get:[InputFile Optional] val script: RegularFileProperty

    /** The initial activity that should be used to start the app. Optional. */
    @get:[Input Optional] val appInitialActivity: Property<String>

    /**
     * The max depth of the traversal stack Robo can explore. Needs to be at least `2` to make Robo
     * explore the app beyond the first activity. Optional; the default is `50`.
     */
    @get:[Input Optional] val maxDepth: Property<Int>

    /** The max number of steps Robo can execute. Optional; the default is no limit. */
    @get:[Input Optional] val maxSteps: Property<Int>

    /**
     * The intents used to launch the app for the crawl. If none are provided, then the main launcher
     * activity is launched. If some are provided, then only those provided are launched (the main
     * launcher activity must be provided explicitly).
     */
    @get:Nested val startingIntents: ListProperty<StartingIntent>

    /** Configures [artifacts][RoboArtifactsHandler] to fetch after completing the test. */
    fun artifacts(configure: Action<in RoboArtifactsHandler>)

    /** Configures the [robo directives][RoboDirectivesHandler] for this test. */
    fun directives(configure: Action<in RoboDirectivesHandler>)

    /**
     * Configures the [starting intents][RoboStartingIntentsHandler] used to launch the app for the crawl.
     * If none are provided, then the main launcher activity is launched. If some are provided, then only those
     * provided are launched (the main launcher activity must be provided explicitly).
     */
    fun startingIntents(configure: Action<in RoboStartingIntentsHandler>)
}

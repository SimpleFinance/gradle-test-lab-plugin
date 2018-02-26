package com.simple.gradle.testlab.model

import groovy.lang.Closure

/**
 * A test of an android application that explores the application on a virtual or physical Android
 * device, finding culprits and crashes as it goes.
 */
interface RoboTest : TestConfig {

    /** The initial activity that should be used to start the app. Optional. */
    var appInitialActivity: String?

    /**
     * The [artifacts][RoboArtifacts] to fetch after completing the test.
     *
     * @see [artifacts]
     */
    override val artifacts: RoboArtifacts

    /**
     * The max depth of the traversal stack Robo can explore. Needs to be at least `2` to make Robo
     * explore the app beyond the first activity. Optional; the default is `50`.
     */
    var maxDepth: Int?

    /** The max number of steps Robo can execute. Optional; the default is no limit. */
    var maxSteps: Int?

    /**
     * A set of directives Robo should apply during the crawl. This allows users to customize the
     * crawl. For example, the username and password for a test account can be provided.
     *
     * @see [roboDirectives]
     */
    val roboDirectives: RoboDirectives

    /** Configure the [artifacts][RoboArtifacts] to fetch after completing the test. */
    fun artifacts(configure: Closure<*>): RoboArtifacts

    /** Configure the [artifacts][RoboArtifacts] to fetch after completing the test. */
    fun artifacts(configure: RoboArtifacts.() -> Unit): RoboArtifacts

    /** Configure the [robo directives][RoboDirectives] for this test. */
    fun roboDirectives(configure: Closure<*>): RoboDirectives

    /** Configure the [robo directives][RoboDirectives] for this test. */
    fun roboDirectives(configure: RoboDirectives.() -> Unit): RoboDirectives
}

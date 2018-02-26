package com.simple.gradle.testlab.model

/**
 * A set of [directives][RoboDirective] Robo should apply during the crawl. This allows users to customize the
 * crawl. For example, the username and password for a test account can be provided.
 */
interface RoboDirectives {
    /**
     * A set of [directives][RoboDirective] Robo should apply during the crawl. This allows users to customize the
     * crawl. For example, the username and password for a test account can be provided.
     */
    val directives: MutableList<out RoboDirective>

    /** Add a `CLICK` directive on the UI element for [resourceName]. */
    fun click(resourceName: String)

    /** Add a `TEXT` directive which inputs [inputText] on the UI element for [resourceName]. */
    fun text(resourceName: String, inputText: String)
}

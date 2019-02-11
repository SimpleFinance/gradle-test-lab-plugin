package com.simple.gradle.testlab.model

/**
 * Directs Robo to interact with a specific UI element if it is encountered during the crawl.
 * Currently, Robo can perform text entry or element clicks.
 */
interface RoboDirective {
    /** The type of action that Robo should perform on the specified element. Required. */
    val actionType: String

    /**
    * The text that Robo is directed to set. If left empty, the directive will be treated as a CLICK
    * on the element matching [resourceName]. Optional.
    */
    val inputText: String?

    /**
     * The android resource name of the target UI element. For example:
     *
     * - in Java: `R.string.foo`
     * - in xml: `@string/foo`
     *
     * Only the `foo` part is needed. See the
     * [reference doc](https://developer.android.com/guide/topics/resources/accessing-resources.html)
     * for more information on accessing resources.
     *
     * Required.
     */
    val resourceName: String
}

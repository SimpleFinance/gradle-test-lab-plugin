package com.simple.gradle.testlab.model

/** The device will be signed into this account for the duration of the test. */
interface Account {
    /** Do not create a Google account for this test. */
    fun none()

    /** Sign in to an automatically-created Google account for the duration of this test. */
    fun googleAuto()
}
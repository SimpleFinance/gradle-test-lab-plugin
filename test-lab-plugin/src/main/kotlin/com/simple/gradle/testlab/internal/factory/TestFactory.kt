package com.simple.gradle.testlab.internal.factory

abstract class TestFactory(
    private val clientName: String,
    private val clientDetails: Map<String, String>
)

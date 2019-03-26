package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.GoogleAuto
import com.google.api.services.testing.model.Account as TestAccount
import com.simple.gradle.testlab.model.Account

internal class DefaultAccount : Account {
    var account: TestAccount? = null

    override fun none() {
        account = null
    }

    override fun googleAuto() {
        account = TestAccount().setGoogleAuto(GoogleAuto())
    }
}
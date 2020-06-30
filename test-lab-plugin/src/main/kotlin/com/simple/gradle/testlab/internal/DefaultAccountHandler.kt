package com.simple.gradle.testlab.internal

import com.google.api.services.testing.model.GoogleAuto
import com.simple.gradle.testlab.model.AccountHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property
import javax.inject.Inject
import com.google.api.services.testing.model.Account as TestAccount

@Suppress("UnstableApiUsage")
internal open class DefaultAccountHandler @Inject constructor(
    objects: ObjectFactory
) : AccountHandler {
    @Input val account: Property<GoogleAccount> = objects.property<GoogleAccount>().value(GoogleAccount.NONE)

    override fun none() {
        account.set(GoogleAccount.NONE)
    }

    override fun googleAuto() {
        account.set(GoogleAccount.GOOGLE_AUTO)
    }
}

internal enum class GoogleAccount(val testAccount: TestAccount?) {
    NONE(null),
    GOOGLE_AUTO(TestAccount().setGoogleAuto(GoogleAuto()))
}

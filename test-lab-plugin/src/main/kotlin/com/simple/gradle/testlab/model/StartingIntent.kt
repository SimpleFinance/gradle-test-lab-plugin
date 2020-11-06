package com.simple.gradle.testlab.model

import com.google.api.services.testing.model.LauncherActivityIntent
import com.google.api.services.testing.model.RoboStartingIntent
import com.google.api.services.testing.model.StartActivityIntent
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

sealed class StartingIntent {
    internal abstract fun asRoboStartingIntent(): RoboStartingIntent

    data class LauncherActivity(
        @get:[Input Optional] val timeout: String?
    ) : StartingIntent() {
        override fun asRoboStartingIntent(): RoboStartingIntent =
            RoboStartingIntent()
                .setLauncherActivity(LauncherActivityIntent())
                .setTimeout(timeout)
    }

    data class StartActivity(
        @get:Input val action: String?,
        @get:[Input Optional] val categories: List<String>?,
        @get:[Input Optional] val uri: String?,
        @get:[Input Optional] val timeout: String?
    ) : StartingIntent() {
        override fun asRoboStartingIntent(): RoboStartingIntent =
            RoboStartingIntent()
                .setStartActivity(
                    StartActivityIntent()
                        .setAction(action)
                        .setCategories(categories?.toList())
                        .setUri(uri)
                )
                .setTimeout(timeout)
    }
}

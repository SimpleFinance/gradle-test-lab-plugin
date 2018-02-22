package com.simple.gradle.testlab.internal

import com.google.api.services.toolresults.model.History

internal class ToolResultsHistoryPicker(
        val projectId: String,
        val googleApi: GoogleApiInternal
) {
    fun pickHistoryName(name: String?, appPackageId: String?): String? =
            name ?: appPackageId?.let { "$it (gradle)" }

    fun getToolResultsHistoryId(name: String?): String? = name?.let {
        getHistoriesByName(name)?.takeUnless { it.isEmpty() }?.get(0)?.historyId
                ?: createHistory(name).historyId
    }

    private fun getHistoriesByName(name: String): List<History>? =
            googleApi.toolResults.projects().histories().list(projectId)
                    .setFilterByName(name)
                    .execute()
                    .histories

    private fun createHistory(name: String): History =
            googleApi.toolResults.projects().histories().create(projectId,
                    History().setName(name).setDisplayName(name))
                    .execute()
}

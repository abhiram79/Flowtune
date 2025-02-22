package com.abhiram.flowtube.models.body

import com.abhiram.flowtube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
)

package com.abhiram.innertube.models.body

import com.abhiram.innertube.models.Context
import com.abhiram.innertube.models.Continuation
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?
)

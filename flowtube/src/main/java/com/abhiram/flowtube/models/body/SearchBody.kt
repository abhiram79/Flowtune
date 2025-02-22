package com.abhiram.flowtube.models.body

import com.abhiram.flowtube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String?,
    val params: String?,
)

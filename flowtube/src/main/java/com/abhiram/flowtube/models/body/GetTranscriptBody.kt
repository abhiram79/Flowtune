package com.abhiram.flowtube.models.body

import com.abhiram.flowtube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)

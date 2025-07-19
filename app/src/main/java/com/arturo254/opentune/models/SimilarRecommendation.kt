package com.abhiram.flowtune.models

import com.arturo254.innertube.models.YTItem
import com.abhiram.flowtune.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)

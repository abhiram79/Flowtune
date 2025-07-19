package com.abhiram.flowtune.models

import com.abhiram.innertube.models.YTItem
import com.abhiram.flowtune.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)

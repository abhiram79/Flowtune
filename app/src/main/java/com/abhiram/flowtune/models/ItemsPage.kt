package com.abhiram.flowtune.models

import com.abhiram.flowtube.models.YTItem

data class ItemsPage(
    val items: List<YTItem>,
    val continuation: String?,
)

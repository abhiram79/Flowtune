package com.abhiram.innertube.pages

import com.abhiram.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)

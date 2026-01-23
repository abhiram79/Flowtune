/**
 * Flowtune YT Music Client (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.abhiram.flowtune.models

import com.metrolist.innertube.models.YTItem
import com.abhiram.flowtune.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)

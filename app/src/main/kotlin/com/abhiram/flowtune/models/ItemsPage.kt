/**
 * Flowtune YT Music Client (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.abhiram.flowtune.models

import com.metrolist.innertube.models.YTItem

data class ItemsPage(
    val items: List<YTItem>,
    val continuation: String?,
)

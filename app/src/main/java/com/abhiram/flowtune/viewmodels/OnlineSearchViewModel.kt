package com.abhiram.flowtune.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhiram.flowtube.YouTube
import com.abhiram.flowtube.models.filterExplicit
import com.abhiram.flowtube.pages.SearchSummaryPage
import com.abhiram.flowtune.constants.HideExplicitKey
import com.abhiram.flowtune.models.ItemsPage
import com.abhiram.flowtune.utils.dataStore
import com.abhiram.flowtune.utils.get
import com.abhiram.flowtune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineSearchViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        val query = savedStateHandle.get<String>("query")!!
        val filter = MutableStateFlow<YouTube.SearchFilter?>(null)
        var summaryPage by mutableStateOf<SearchSummaryPage?>(null)
        val viewStateMap = mutableStateMapOf<String, ItemsPage?>()

        init {
            viewModelScope.launch {
                filter.collect { filter ->
                    if (filter == null) {
                        if (summaryPage == null) {
                            YouTube
                                .searchSummary(query)
                                .onSuccess {
                                    summaryPage =
                                        it.filterExplicit(
                                            context.dataStore.get(
                                                HideExplicitKey,
                                                false,
                                            ),
                                        )
                                }.onFailure {
                                    reportException(it)
                                }
                        }
                    } else {
                        if (viewStateMap[filter.value] == null) {
                            YouTube
                                .search(query, filter)
                                .onSuccess { result ->
                                    viewStateMap[filter.value] =
                                        ItemsPage(
                                            result.items
                                                .distinctBy { it.id }
                                                .filterExplicit(context.dataStore.get(HideExplicitKey, false)),
                                            result.continuation,
                                        )
                                }.onFailure {
                                    reportException(it)
                                }
                        }
                    }
                }
            }
        }

        fun loadMore() {
            val filter = filter.value?.value
            viewModelScope.launch {
                if (filter == null) return@launch
                val viewState = viewStateMap[filter] ?: return@launch
                val continuation = viewState.continuation
                if (continuation != null) {
                    val searchResult = YouTube.searchContinuation(continuation).getOrNull() ?: return@launch
                    viewStateMap[filter] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
                }
            }
        }
    }

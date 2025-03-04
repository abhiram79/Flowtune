package com.abhiram.flowtune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhiram.flowtube.YouTube
import com.abhiram.flowtube.models.PlaylistItem
import com.abhiram.flowtune.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
    @Inject
    constructor() : ViewModel() {
        val playlists = MutableStateFlow<List<PlaylistItem>?>(null)

        init {
            viewModelScope.launch {
                YouTube
                    .likedPlaylists()
                    .onSuccess {
                        playlists.value = it
                    }.onFailure {
                        reportException(it)
                    }
            }
        }
    }

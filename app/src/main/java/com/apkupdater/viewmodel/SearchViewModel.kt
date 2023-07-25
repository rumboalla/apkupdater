package com.apkupdater.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.repository.SearchRepository
import com.apkupdater.util.launchWithMutex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

class SearchViewModel(
    private val mainViewModel: MainViewModel,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val mutex = Mutex()
    private val state = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))

    fun state(): StateFlow<SearchUiState> = state

    fun search(text: String) = viewModelScope.launchWithMutex(mutex, Dispatchers.IO) {
        state.value = SearchUiState.Loading
        mainViewModel.changeSearchBadge("")
        searchRepository.search(text).collect {
            it.onSuccess { apps ->
                state.value = SearchUiState.Success(apps)
                mainViewModel.changeSearchBadge(apps.size.toString())
            }.onFailure {
                mainViewModel.changeSearchBadge("!")
                state.value = SearchUiState.Error
            }
        }
    }

}

package com.apkupdater.data.ui


sealed class SearchUiState {
    data object Loading: SearchUiState()
    data object Error : SearchUiState()
    data class Success(val updates: List<AppUpdate>): SearchUiState()

    inline fun onLoading(block: (Loading) -> Unit): SearchUiState {
        if (this is Loading) block(this)
        return this
    }

    inline fun onError(block: (Error) -> Unit): SearchUiState {
        if (this is Error) block(this)
        return this
    }

    inline fun onSuccess(block: (Success) -> Unit): SearchUiState {
        if (this is Success) block(this)
        return this
    }

    fun mutableUpdates(): MutableList<AppUpdate> {
        if (this is Success) {
            return updates.toMutableList()
        }
        return mutableListOf()
    }

}

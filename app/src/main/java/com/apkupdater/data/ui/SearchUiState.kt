package com.apkupdater.data.ui


sealed class SearchUiState {
    object Error : SearchUiState()
    class Success(val loading: Boolean, val updates: List<AppUpdate>): SearchUiState()

    inline fun onError(block: (Error) -> Unit): SearchUiState {
        if (this is Error) block(this)
        return this
    }

    inline fun onSuccess(block: (Success) -> Unit): SearchUiState {
        if (this is Success) block(this)
        return this
    }
}

package com.apkupdater.data.ui


sealed class SearchUiState {
    object Loading: SearchUiState()
    object Error : SearchUiState()
    class Success(val updates: List<AppUpdate>, ): SearchUiState()

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
}

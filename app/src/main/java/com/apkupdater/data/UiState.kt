package com.apkupdater.data


sealed class UiState {
	object Loading: UiState()
	object Error : UiState()
	class Success(val apps: List<AppInstalled>, val excludeSystem: Boolean): UiState()

	inline fun onLoading(block: (Loading) -> Unit): UiState {
		if (this is Loading) block(this)
		return this
	}

	inline fun onError(block: (Error) -> Unit): UiState {
		if (this is Error) block(this)
		return this
	}

	inline fun onSuccess(block: (Success) -> Unit): UiState {
		if (this is Success) block(this)
		return this
	}
}

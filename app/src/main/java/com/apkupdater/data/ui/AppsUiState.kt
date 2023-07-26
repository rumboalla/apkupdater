package com.apkupdater.data.ui


sealed class AppsUiState {
	object Loading: AppsUiState()
	object Error : AppsUiState()
	class Success(
		val apps: List<AppInstalled>,
		val excludeSystem: Boolean,
		val excludeAppStore: Boolean,
		val excludeDisabled: Boolean
	): AppsUiState()

	inline fun onLoading(block: (Loading) -> Unit): AppsUiState {
		if (this is Loading) block(this)
		return this
	}

	inline fun onError(block: (Error) -> Unit): AppsUiState {
		if (this is Error) block(this)
		return this
	}

	inline fun onSuccess(block: (Success) -> Unit): AppsUiState {
		if (this is Success) block(this)
		return this
	}
}

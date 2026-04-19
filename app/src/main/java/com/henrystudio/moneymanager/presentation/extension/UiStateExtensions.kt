package com.henrystudio.moneymanager.presentation.extension

import com.henrystudio.moneymanager.presentation.model.UiState

inline fun <T> UiState<T>.handle(
    onLoading: () -> Unit = {},
    onEmpty: () -> Unit = {},
    onError: (String) -> Unit = {},
    onSuccess: (T) -> Unit = {}
) {
    when (this) {
        is UiState.Loading -> onLoading()
        is UiState.Empty -> onEmpty()
        is UiState.Error -> onError(message)
        is UiState.Success -> onSuccess(data)
    }
}


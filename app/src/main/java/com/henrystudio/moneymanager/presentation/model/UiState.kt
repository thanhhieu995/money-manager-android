package com.henrystudio.moneymanager.presentation.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed class UiState<out T> {
    object Loading: UiState<Nothing>()
    object Empty: UiState<Nothing>()
    data class Success<out T>(val data: T): UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

fun <T> Flow<List<T>>.toUiState(): Flow<UiState<List<T>>> =
    this.map { list ->
        if (list.isEmpty()) UiState.Empty else UiState.Success(list)
    }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
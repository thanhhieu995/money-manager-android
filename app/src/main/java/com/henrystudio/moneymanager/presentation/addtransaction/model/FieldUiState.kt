package com.henrystudio.moneymanager.presentation.addtransaction.model

data class FieldUiState(
    val text: String = "",
    val state: FieldState = FieldState.IDLE,
    val isFocused: Boolean = false,
    val isTouched: Boolean = false
)
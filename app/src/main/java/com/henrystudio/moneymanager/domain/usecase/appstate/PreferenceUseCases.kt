package com.henrystudio.moneymanager.domain.usecase.appstate
data class PreferenceUseCases(
    val saveLastDate: SaveLastDateUseCase,
    val getLastDate: GetLastDateUseCase
)

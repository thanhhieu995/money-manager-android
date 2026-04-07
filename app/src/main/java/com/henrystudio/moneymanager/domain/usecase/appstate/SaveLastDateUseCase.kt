package com.henrystudio.moneymanager.domain.usecase.appstate

import com.henrystudio.moneymanager.domain.repository.LastDateRepository
import java.time.LocalDate

class SaveLastDateUseCase(
    private val repository: LastDateRepository
) {
    operator fun invoke(date: LocalDate) {
        return repository.saveLastDate(date)
    }
}
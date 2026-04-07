package com.henrystudio.moneymanager.domain.usecase.appstate

import com.henrystudio.moneymanager.domain.repository.LastDateRepository
import java.time.LocalDate

class GetLastDateUseCase(
    private val repository: LastDateRepository
) {
    operator fun invoke(): LocalDate? {
        return repository.getLastDate()
    }
}
package com.henrystudio.moneymanager.domain.usecase.category

import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import javax.inject.Inject

class IncreaseCategoryUsageUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
        suspend operator fun invoke(categoryId: Int) {
            repository.increaseUsageCount(categoryId)
        }
}
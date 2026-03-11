package com.henrystudio.moneymanager.domain.usecase.category

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.domain.repository.CategoryRepository

class InsertCategoryUseCase(
    private val repository: CategoryRepository
) {

    suspend operator fun invoke(category: Category) {
        repository.insert(category)
    }

}
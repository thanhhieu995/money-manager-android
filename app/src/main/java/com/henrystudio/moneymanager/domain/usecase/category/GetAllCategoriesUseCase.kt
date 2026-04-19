package com.henrystudio.moneymanager.domain.usecase.category

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetAllCategoriesUseCase(
    private val repository: CategoryRepository
) {

    operator fun invoke(): Flow<List<Category>> {
        return repository.getAll()
    }
}
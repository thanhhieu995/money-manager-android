package com.henrystudio.moneymanager.domain.usecase.category

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import com.henrystudio.moneymanager.presentation.model.TransactionType
import kotlinx.coroutines.flow.Flow

class GetCategoriesByTypeUseCase(
    private val repository: CategoryRepository
) {

    operator fun invoke(type: TransactionType): Flow<List<Category>> {
        return repository.getCategoriesByType(type)
    }
}
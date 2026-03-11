package com.henrystudio.moneymanager.domain.usecase.category

import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetParentCategoriesUseCase(
    private val repository: CategoryRepository
) {

    operator fun invoke(type: CategoryType): Flow<List<Category>> {
        return repository.getParentCategories(type)
    }

}
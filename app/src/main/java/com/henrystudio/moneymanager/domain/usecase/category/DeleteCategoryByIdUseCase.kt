package com.henrystudio.moneymanager.domain.usecase.category

import com.henrystudio.moneymanager.domain.repository.CategoryRepository

class DeleteCategoryByIdUseCase(
    private val repository: CategoryRepository
) {

    suspend operator fun invoke(id: Int) {
        repository.deleteId(id)
    }

}
package com.henrystudio.moneymanager.domain.usecase.category

data class CategoryUseCases(

    val getParentCategories: GetParentCategoriesUseCase,

    val getChildCategories: GetChildCategoriesUseCase,

    val getAllCategories: GetAllCategoriesUseCase,

    val getCategoriesByType: GetCategoriesByTypeUseCase,

    val insertCategory: InsertCategoryUseCase,

    val deleteCategory: DeleteCategoryUseCase,

    val deleteCategoryById: DeleteCategoryByIdUseCase,

    val updateCategory: UpdateCategoryUseCase,

    val increaseCategoryUsage: IncreaseCategoryUsageUseCase
)
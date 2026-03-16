package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.presentation.views.addtransaction.CategoryItem
import com.henrystudio.moneymanager.presentation.views.addtransaction.CategoryDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val categoryUseCases: CategoryUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    fun loadChildCategories(parentId: Int, parentName: String?, parentEmoji: String?) {
        viewModelScope.launch {
            categoryUseCases.getChildCategories(parentId).collect { list ->
                val items = list.map { category ->
                    categoryToCategoryItem(category, parentName, parentEmoji)
                }
                _uiState.update { it.copy(categoryItems = items) }
            }
        }
    }

    private fun categoryToCategoryItem(
        category: Category,
        parentName: String?,
        parentEmoji: String?
    ): CategoryItem = CategoryItem(
        id = category.id,
        name = category.name,
        emoji = category.emoji,
        parentId = category.parentId ?: -1,
        isParent = false,
        parentName = parentName,
        parentEmoji = parentEmoji
    )

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            categoryUseCases.deleteCategoryById(id)
        }
    }
}

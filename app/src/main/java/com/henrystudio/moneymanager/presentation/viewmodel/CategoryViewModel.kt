package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.model.toUiState
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor (private val categoryUseCases: CategoryUseCases) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories
    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    val categoryState: StateFlow<UiState<List<Category>>> =
        _selectedType.filterNotNull()
            .flatMapLatest { type ->
                categoryUseCases.getCategoriesByType(type)
            }
            .toUiState()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UiState.Loading
            )
    fun getParentCategories(type: TransactionType): StateFlow<List<Category>> =
        categoryUseCases.getParentCategories(type).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getChildCategories(parentId: Int): StateFlow<List<Category>> =
        categoryUseCases.getChildCategories(parentId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getAll(): StateFlow<List<Category>> =
        categoryUseCases.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getCategoriesByType(type: TransactionType): StateFlow<UiState<List<Category>>> =
        categoryUseCases.getCategoriesByType(type)
            .map { list ->
                if (list.isEmpty()) UiState.Empty
                else UiState.Success(list)
            }
            .onStart { emit(UiState.Loading) }
            .catch { emit(UiState.Error(it.message ?: "Unknow error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    fun insert(category: Category) = viewModelScope.launch {
        categoryUseCases.insertCategory(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        categoryUseCases.deleteCategory(category)
    }

    fun deleteId(id: Int) = viewModelScope.launch {
        categoryUseCases.deleteCategoryById(id)
    }

    fun update(category: Category) = viewModelScope.launch {
        categoryUseCases.updateCategory(category)
    }

    fun updateChildren(children: List<Category>) = viewModelScope.launch {
        children.forEach{categoryUseCases.updateCategory(it)}
    }

    fun setType(type: TransactionType) {
        _selectedType.value = type
    }
}

package com.henrystudio.moneymanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.presentation.views.addtransaction.EditItem
import com.henrystudio.moneymanager.presentation.views.addtransaction.EditItemDialogUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditItemDialogViewModel @Inject constructor(
    private val categoryUseCases: CategoryUseCases,
    private val accountUseCases: AccountUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditItemDialogUiState())
    val uiState: StateFlow<EditItemDialogUiState> = _uiState.asStateFlow()

    fun loadItems(selectedType: CategoryType?) {
        if (selectedType == null) {
            viewModelScope.launch {
                accountUseCases.getAccountsUseCase().collect { accounts ->
                    val items = accounts.map { EditItem.AccountItem(it) }
                    _uiState.update { it.copy(editItems = items) }
                }
            }
        } else {
            viewModelScope.launch {
                categoryUseCases.getCategoriesByType(selectedType).collect { list ->
                    val treeItems = Helper.buildCategoryTree(list)
                    val items = treeItems.map { EditItem.Category(it) }
                    _uiState.update { it.copy(editItems = items) }
                }
            }
        }
    }

    fun deleteItem(item: EditItem) {
        viewModelScope.launch {
            when (item) {
                is EditItem.Category -> categoryUseCases.deleteCategoryById(item.item.id)
                is EditItem.AccountItem -> accountUseCases.deleteAccountUseCase(item.item)
            }
        }
    }
}

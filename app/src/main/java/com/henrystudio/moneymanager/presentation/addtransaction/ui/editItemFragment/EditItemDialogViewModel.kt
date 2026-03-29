package com.henrystudio.moneymanager.presentation.addtransaction.ui.editItemFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.presentation.addtransaction.model.EditItem
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditItemDialogViewModel @Inject constructor(
    private val categoryUseCases: CategoryUseCases,
    private val accountUseCases: AccountUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditItemDialogUiState())
    val uiState: StateFlow<EditItemDialogUiState> = _uiState.asStateFlow()
    private val selectedType = MutableStateFlow<ItemType?>(null)
    private val transactionType = MutableStateFlow<TransactionType?>(null)

    val editItems: StateFlow<List<EditItem>> =
        combine(selectedType, transactionType) { type, transactionType ->
            type to transactionType

            }.flatMapLatest { (type, transactionType) ->
            when (type) {
                ItemType.ACCOUNT -> {
                    accountUseCases.getAccountsUseCase()
                        .map { list -> list.map { EditItem.AccountItem(it) } }
                }

                ItemType.CATEGORY -> {
                    if (transactionType == null) return@flatMapLatest flowOf(emptyList())

                    categoryUseCases.getCategoriesByType(transactionType)
                        .map { list ->
                            val tree = Helper.buildCategoryTree(list)
                            tree.map { EditItem.Category(it) }
                        }
                }

                else -> flowOf(emptyList())
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setType(type: ItemType?, typeTransaction: TransactionType?) {
        selectedType.value = type
        transactionType.value = typeTransaction
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

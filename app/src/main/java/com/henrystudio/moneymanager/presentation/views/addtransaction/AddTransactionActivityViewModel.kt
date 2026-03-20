package com.henrystudio.moneymanager.presentation.views.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.presentation.model.AddItemSource
import com.henrystudio.moneymanager.presentation.model.ItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionActivityViewModel @Inject constructor() : ViewModel() {
    private val _toolbarState = MutableStateFlow(AddTransactionToolbarState())
    val toolbarState : StateFlow<AddTransactionToolbarState> = _toolbarState
    private val _event = MutableSharedFlow<AddItemAction>()
    val event = _event.asSharedFlow()
    private var currentAction: AddItemAction? = null

    fun onEnterAddItem(title: String) {
        _toolbarState.value = AddTransactionToolbarState(
            title = title,
            showAddIcon = false,
            showBookmarkIcon = false,
            animation = TitleAnimation.SlideFromRight
        )
    }

    fun onBackToRoot(isIncome: Boolean) {
        _toolbarState.value = AddTransactionToolbarState(
            title = if (isIncome) "Income" else "Expense",
            showAddIcon = false,
            showBookmarkIcon = true,
            animation = TitleAnimation.SlideFromLeft
        )
    }

    fun onRootScreen(isIncome: Boolean) {
        _toolbarState.value = AddTransactionToolbarState(
            title = if (isIncome) "Income" else "Expense",
            showAddIcon = false,
            showBookmarkIcon = true,
            animation = TitleAnimation.None
        )
    }

    fun onAddItemClicked(
       action: AddItemAction
    ) {
        currentAction = action
        val title = when (action) {
            is AddItemAction.FromAddTransaction -> "Add"
            is AddItemAction.FromEditCategory -> action.categoryName
            is AddItemAction.FromEditAccount -> action.accountName
            is AddItemAction.FromCategoryDetail -> "Category"
        }

        _toolbarState.value = AddTransactionToolbarState(
            title = title,
            showAddIcon = false,
            showBookmarkIcon = false,
            animation = TitleAnimation.SlideFromRight
        )

        viewModelScope.launch {
            _event.emit(action)
        }
    }

    fun onAddIconClicked() {
        currentAction?.let {
            _event.tryEmit(it)
        }
    }
}
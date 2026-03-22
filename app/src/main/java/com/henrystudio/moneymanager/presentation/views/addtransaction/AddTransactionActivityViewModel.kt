package com.henrystudio.moneymanager.presentation.views.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _event = MutableSharedFlow<AddTransactionEvent>()
    val event = _event.asSharedFlow()
    private var currentAction: AddItemAction? = null
    private var currentItemType: ItemType? = null

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
       action: AddItemAction,
       itemType: ItemType
    ) {
        currentAction = action
        currentItemType = itemType   // 🔥 BẮT BUỘC

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
            _event.emit(AddTransactionEvent.NavigateToAddItem(action, itemType))
        }
    }

    fun onEditItemClicked(
        action: AddItemAction,
        itemType: ItemType
    ) {
        currentAction = action
        currentItemType = itemType

        val title = when (action) {
            is AddItemAction.FromEditCategory -> action.categoryName
            is AddItemAction.FromEditAccount -> action.accountName
            else -> "Edit"
        }

        _toolbarState.value = AddTransactionToolbarState(
            title = title,
            showAddIcon = false,
            showBookmarkIcon = false,
            animation = TitleAnimation.SlideFromRight
        )

        viewModelScope.launch {
            _event.emit(
                AddTransactionEvent.NavigateToEditItem(action)
            )
        }
    }

    fun onAddIconClicked() {
        val action = currentAction ?: return
        val itemType = currentItemType ?: return

        _event.tryEmit(
            AddTransactionEvent.NavigateToAddItem(
                itemType = itemType,
                action = action
            )
        )
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _event.emit(AddTransactionEvent.PopBack)
        }
    }
}
package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionActivityViewModel @Inject constructor() : ViewModel() {
    private val _toolbarState = MutableStateFlow(AddTransactionToolbarState(title = ToolbarTitle.EXPENSE, animation = TitleAnimation.None, config = toolbarConfig()))
    val toolbarState : StateFlow<AddTransactionToolbarState> = _toolbarState
    private val _event = MutableSharedFlow<AddTransactionEvent>()
    val event = _event.asSharedFlow()
    private var currentAction: AddItemAction? = null
    var currentItemType: ItemType? = null
    private var isIncome: Boolean = false
    var transactionType: TransactionType = TransactionType.EXPENSE
    private val titleStack = ArrayDeque<ToolbarTitle>()

    fun init(transaction: Transaction?) {
        val root = if (transaction?.isIncome == true) {
            ToolbarTitle.INCOME
        } else {
            ToolbarTitle.EXPENSE
        }

        titleStack.clear()
        titleStack.add(root)

        _toolbarState.value = AddTransactionToolbarState(
            title = root,
            animation = TitleAnimation.None,
            config = toolbarConfig(showBookmark = true)
        )
    }

    fun onEnterAddItem(title: String) {
        _toolbarState.value = AddTransactionToolbarState(
            title = (if (isIncome) ToolbarTitle.INCOME else ToolbarTitle.EXPENSE),
            animation = TitleAnimation.SlideFromRight,
            config = toolbarConfig()
        )
    }

    fun onBackToRoot() {
        _toolbarState.value = AddTransactionToolbarState(
            title = (if (isIncome) ToolbarTitle.INCOME else ToolbarTitle.EXPENSE),
            animation = TitleAnimation.SlideFromLeft,
            config = toolbarConfig()
        )
    }

    fun onRootScreen() {
        _toolbarState.value = AddTransactionToolbarState(
            title = (if (isIncome) ToolbarTitle.INCOME else ToolbarTitle.EXPENSE),
            animation = TitleAnimation.None,
            config = toolbarConfig()
        )
    }

    fun onAddItemClicked(
        action: AddItemAction,
        itemType: ItemType
    ) {
        currentAction = action
        currentItemType = itemType

        val newTitle = when (action) {
            is AddItemAction.FromAddTransaction -> ToolbarTitle.ADD
            is AddItemAction.FromEditCategory -> ToolbarTitle.CATEGORY
            is AddItemAction.FromEditAccount -> ToolbarTitle.EDIT
            is AddItemAction.FromCategoryDetail -> ToolbarTitle.CATEGORY
        }

        titleStack.addLast(newTitle)

        _toolbarState.value = AddTransactionToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = toolbarConfig()
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

        val newTitle = ToolbarTitle.EDIT

        titleStack.addLast(newTitle)

        _toolbarState.value = AddTransactionToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = toolbarConfig()
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToEditItem(action))
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

    private fun toolbarConfig(
        showAdd: Boolean = false,
        showBookmark: Boolean = true,
        alignTitleToBack: Boolean = false
    ) = ToolbarConfig(
        showAdd = showAdd,
        showBookmark = showBookmark,
        alignTitleToBack = alignTitleToBack
    )

    fun transactionTypeChanged(type: TransactionType) {
        transactionType = type

        val newRoot = when (type) {
            TransactionType.INCOME -> ToolbarTitle.INCOME
            TransactionType.EXPENSE -> ToolbarTitle.EXPENSE
        }

        // 🔥 reset stack
        titleStack.clear()
        titleStack.add(newRoot)

        _toolbarState.value = AddTransactionToolbarState(
            title = newRoot,
            animation = TitleAnimation.None,
            config = toolbarConfig(
                showAdd = false,
                showBookmark = true
            )
        )
    }

    fun onBackClicked() {
        if (titleStack.size > 1) {
            titleStack.removeLast()

            val newCurrent = titleStack.last()

            _toolbarState.value = AddTransactionToolbarState(
                title = newCurrent,
                animation = TitleAnimation.SlideFromLeft,
                config = toolbarConfig(showBookmark = titleStack.size == 1)
            )
        }

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.PopBack)
        }
    }

    fun onSaveItem() {
        if (titleStack.size > 1) {
            titleStack.removeLast()

            _toolbarState.value = AddTransactionToolbarState(
                title = titleStack.last(),
                animation = TitleAnimation.SlideFromLeft,
                config = toolbarConfig(showBookmark = titleStack.size == 1)
            )
        }

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.PopBack)
        }
    }
}
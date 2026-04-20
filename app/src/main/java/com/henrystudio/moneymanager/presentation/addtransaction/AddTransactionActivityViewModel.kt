package com.henrystudio.moneymanager.presentation.addtransaction

import android.content.ClipData.Item
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddTransactionEvent
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.EditItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.ToolbarStateStack
import com.henrystudio.moneymanager.presentation.addtransaction.model.ToolbarTitle
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
    var currentAction: AddItemAction? = null
    var currentItemType: ItemType? = null
    private val _transactionType = MutableStateFlow(TransactionType.EXPENSE)
    var transactionType: StateFlow<TransactionType> = _transactionType
    private val toolbarStack = ArrayDeque<ToolbarStateStack>()
    var currentParentCategoryId: Int? = null
    fun init(transaction: Transaction?) {
        val root = if (transaction?.isIncome == true) {
            ToolbarTitle.INCOME
        } else {
            ToolbarTitle.EXPENSE
        }

        val rootConfig = toolbarConfig(showBookmark = true)
        setRootToolbarState(root, rootConfig)
    }

    fun onAddItemClicked(
        action: AddItemAction,
        itemType: ItemType,
        editItem: EditItem? = null
    ) {
        currentAction = action
        currentItemType = itemType

        val newTitle = when (action) {
            is AddItemAction.FromAddTransaction -> ToolbarTitle.ADD
            is AddItemAction.FromEditCategory -> ToolbarTitle.CATEGORY
            is AddItemAction.FromEditAccount -> ToolbarTitle.ACCOUNT
            is AddItemAction.FromCategoryDetail -> ToolbarTitle.CATEGORY
        }

        val newConfig = toolbarConfig(showAdd = false).copy(
            addAction = action,
            addItemType = itemType
        )

        pushToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = newConfig
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToAddItem(action, itemType, editItem))
        }
    }

    fun onEditItemClicked(
        action: AddItemAction,
        itemType: ItemType
    ) {
        currentAction = action
        currentItemType = itemType

        val newTitle = when (action) {
            is AddItemAction.FromAddTransaction -> ToolbarTitle.ADD
            is AddItemAction.FromEditCategory -> ToolbarTitle.CATEGORY
            is AddItemAction.FromEditAccount -> ToolbarTitle.ACCOUNT
            is AddItemAction.FromCategoryDetail -> ToolbarTitle.CATEGORY
        }

        val newConfig = toolbarConfig(showAdd = true).copy(
            addAction = action,
            addItemType = itemType
        )

        pushToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = newConfig
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToEditItem(action, itemType, transactionType.value))
        }
    }

    fun onAddIconClicked() {
        val config = _toolbarState.value.config
        val action = when (config.addAction) {

            is AddItemAction.FromEditAccount -> {
                // đang ở Account → Add account mới
                AddItemAction.FromEditAccount
            }

            is AddItemAction.FromEditCategory -> {
                AddItemAction.FromEditCategory
            }

            is AddItemAction.FromCategoryDetail -> {
                AddItemAction.FromCategoryDetail
            }

            else -> {
                AddItemAction.FromAddTransaction
            }
        }
        val itemType = config.addItemType ?: return

        val currentTitle = toolbarStack.lastOrNull()?.title

        val newTitle = when (action) {
            is AddItemAction.FromAddTransaction -> ToolbarTitle.ADD
            is AddItemAction.FromEditCategory -> ToolbarTitle.CATEGORY
            is AddItemAction.FromEditAccount -> ToolbarTitle.ACCOUNT

            is AddItemAction.FromCategoryDetail -> {
                // 🔥 dùng title hiện tại
                when (currentTitle) {
                    is ToolbarTitle.Custom -> currentTitle
                    else -> ToolbarTitle.CATEGORY
                }
            }
        }

        val newConfig = toolbarConfig(showAdd = false).copy(
            addAction = action,
            addItemType = itemType
        )

        pushToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = newConfig
        )

        viewModelScope.launch {
            _event.emit(
                AddTransactionEvent.NavigateToAddItem(
                    itemType = itemType,
                    action = action,
                    editItem = null
                )
            )
        }
    }

    private fun toolbarConfig(
        showAdd: Boolean = false,
        showBookmark: Boolean = false,
        alignTitleToBack: Boolean = false
    ) = ToolbarConfig(
        showAdd = showAdd,
        showBookmark = showBookmark,
        alignTitleToBack = alignTitleToBack
    )

    fun transactionTypeChanged(type: TransactionType) {
        _transactionType.value = type

        val newRoot = when (type) {
            TransactionType.INCOME -> ToolbarTitle.INCOME
            TransactionType.EXPENSE -> ToolbarTitle.EXPENSE
        }

        val rootConfig = toolbarConfig(
            showAdd = false,
            showBookmark = true
        )

        setRootToolbarState(newRoot, rootConfig)
    }

    fun onBackClicked() {
        if (toolbarStack.size > 1) {
            toolbarStack.removeLast()

            val previous = toolbarStack.last()

            _toolbarState.value = AddTransactionToolbarState(
                title = previous.title,
                animation = TitleAnimation.SlideFromLeft,
                config = previous.config
            )
        }

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.PopBack)
        }
    }

    fun onSaveItem() {
        if (toolbarStack.size > 1) {
            toolbarStack.removeLast()

            val previous = toolbarStack.last()

            _toolbarState.value = AddTransactionToolbarState(
                title = previous.title,
                animation = TitleAnimation.SlideFromLeft,
                config = previous.config
            )
        }

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.PopBack)
        }
    }

    fun onRootCategoryItemClicked(item: CategoryItem, action: AddItemAction) {
        val title = item.name
        val newTitle = ToolbarTitle.Custom(title)

        val newConfig = toolbarConfig(showAdd = true).copy(
            addAction = action,
            addItemType = ItemType.CATEGORY
        )

        pushToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = newConfig
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToCategoryDetailWithTitle(EditItem.Category(item), action, title))
        }
    }

    private fun setRootToolbarState(
        title: ToolbarTitle,
        config: ToolbarConfig
    ) {
        toolbarStack.clear()
        toolbarStack.addLast(
            ToolbarStateStack(
                title = title,
                config = config
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
            title = title,
            animation = TitleAnimation.None,
            config = config
        )
    }

    private fun pushToolbarState(
        title: ToolbarTitle,
        animation: TitleAnimation,
        config: ToolbarConfig
    ) {
        toolbarStack.addLast(
            ToolbarStateStack(
                title = title,
                config = config
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
            title = title,
            animation = animation,
            config = config
        )
    }
}

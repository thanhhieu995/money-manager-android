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
    private var isIncome: Boolean = false
    var transactionType: TransactionType = TransactionType.EXPENSE
    private val toolbarStack = ArrayDeque<ToolbarStateStack>()
    var currentParentCategoryId: Int? = null
    fun init(transaction: Transaction?) {
        val root = if (transaction?.isIncome == true) {
            ToolbarTitle.INCOME
        } else {
            ToolbarTitle.EXPENSE
        }

        toolbarStack.clear()
        toolbarStack.add(
            ToolbarStateStack(
                title = root,
                config = toolbarConfig(showBookmark = true)
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
            title = root,
            animation = TitleAnimation.None,
            config = toolbarConfig(showBookmark = true)
        )
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

        toolbarStack.addLast(
            ToolbarStateStack(
                title = newTitle,
                config = newConfig
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
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

        toolbarStack.addLast(
            ToolbarStateStack(
                title = newTitle,
                config = toolbarConfig(showAdd = true).copy(
                    addAction = action,
                    addItemType = itemType
                )
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = toolbarConfig(showAdd = true).copy(
                addAction = action,
                addItemType = itemType
            )
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToEditItem(action, itemType, transactionType))
        }
    }

    fun onAddIconClicked() {
        val config = _toolbarState.value.config
        Log.d("DEBUG", "onAddIconClicked: $config")
        val action = config.addAction ?: return
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

        val newConfig = toolbarConfig().copy(
            addAction = action,
            addItemType = itemType
        )

        toolbarStack.addLast(
            ToolbarStateStack(
                title = newTitle,
                config = newConfig
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
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
        transactionType = type

        val newRoot = when (type) {
            TransactionType.INCOME -> ToolbarTitle.INCOME
            TransactionType.EXPENSE -> ToolbarTitle.EXPENSE
        }

        // 🔥 reset stack
        toolbarStack.clear()
        toolbarStack.addLast(
            ToolbarStateStack(
                title = newRoot,
                config = toolbarConfig(showAdd = true)
            )
        )

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

        toolbarStack.addLast(
            ToolbarStateStack(
                title = newTitle,
                config = newConfig
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = newConfig
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToCategoryDetailWithTitle(EditItem.Category(item), action, title))
        }
    }

    fun onChildCategoryClicked(item: CategoryItem) {
        val title = item.name
        val newTitle = ToolbarTitle.Custom(title)
        toolbarStack.addLast(
            ToolbarStateStack(
                title = newTitle,
                config = toolbarConfig(showAdd = true)
            )
        )

        _toolbarState.value = AddTransactionToolbarState(
            title = newTitle,
            animation = TitleAnimation.SlideFromRight,
            config = toolbarConfig(showAdd = true)
        )

        viewModelScope.launch {
            _event.emit(AddTransactionEvent.NavigateToCategoryDetailWithTitle(EditItem.Category(item), AddItemAction.FromCategoryDetail
            , title))
        }
    }

//    fun updateToolbarForCategoryDetail(action: AddItemAction) {
//
//        val config = toolbarConfig().copy(
//            showAdd = true,
//            addAction = action,
//            addItemType = ItemType.CATEGORY
//        )
//
//        _toolbarState.value = _toolbarState.value.copy(config = config)
//    }
}
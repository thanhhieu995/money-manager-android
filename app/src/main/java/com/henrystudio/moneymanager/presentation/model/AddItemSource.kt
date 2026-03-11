package com.henrystudio.moneymanager.presentation.model

import java.io.Serializable

enum class AddItemSource: Serializable {
    FROM_ADD_TRANSACTION,
    FROM_EDIT_ITEM_CATEGORY_DIALOG,
    FROM_EDIT_ITEM_ACCOUNT_DIALOG,
    FROM_DETAIL_CATEGORY
}
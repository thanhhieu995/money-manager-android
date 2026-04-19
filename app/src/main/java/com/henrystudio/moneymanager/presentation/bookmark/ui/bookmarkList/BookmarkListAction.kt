package com.henrystudio.moneymanager.presentation.bookmark.ui.bookmarkList

import com.henrystudio.moneymanager.data.model.Transaction

sealed class BookmarkListAction {
    data class DeleteBookmark(val transaction: Transaction) : BookmarkListAction()
}
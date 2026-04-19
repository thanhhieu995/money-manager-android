package com.henrystudio.moneymanager.presentation.bookmark.ui.addBookmark

import com.henrystudio.moneymanager.data.model.Transaction

sealed class AddBookmarkAction{
    data class BookmarkClicked(val transaction: Transaction): AddBookmarkAction()
}

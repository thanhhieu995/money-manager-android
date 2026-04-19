package com.henrystudio.moneymanager.presentation.bookmark.ui.addBookmark

sealed class AddBookmarkEvent{
     data object NavigationBack : AddBookmarkEvent()
}

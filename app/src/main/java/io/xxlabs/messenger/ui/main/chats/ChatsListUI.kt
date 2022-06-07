package io.xxlabs.messenger.ui.main.chats


interface ChatsListUI {
    val newConnectionsVisible: Boolean
    val searchVisible: Boolean
    val emptyPlaceholderVisible: Boolean
    val noResultsFoundVisible: Boolean
    val noResultsFoundName: String?
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
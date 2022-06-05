package io.xxlabs.messenger.ui.main.chats

interface ChatsListUI {
    val newConnectionsVisible: Boolean
    val searchVisible: Boolean
    val emptyPlaceholderVisible: Boolean
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
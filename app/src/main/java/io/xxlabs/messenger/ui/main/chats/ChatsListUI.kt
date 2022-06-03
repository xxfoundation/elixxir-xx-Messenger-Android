package io.xxlabs.messenger.ui.main.chats

interface ChatsListUI {
    val newConnectionsVisible: Boolean
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
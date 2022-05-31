package io.xxlabs.messenger.ui.main.chats

interface ChatsListUI {
    val hasNewConnections: Boolean
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
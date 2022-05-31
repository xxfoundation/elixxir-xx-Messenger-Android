package io.xxlabs.messenger.ui.main.chats

interface ChatsListListener {
    fun onSearchResultsUpdated(visible: Boolean)
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
package io.xxlabs.messenger.ui.main.chats

interface ChatsListListener {
    fun onSearchHasFocus(focus: Boolean)
    fun onSearchResultsUpdated(visible: Boolean)
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
package io.xxlabs.messenger.ui.main.chats

class ChatsList(
    private val listener: ChatsListListener,
    override val newConnectionsVisible: Boolean = false,
    override val searchVisible: Boolean = false,
    override val emptyPlaceholderVisible: Boolean = true,
    override val noResultsFoundVisible: Boolean = false,
    override val noResultsFoundName: String? = null
) : ChatsListUI {
    override fun onAddContactClicked() = listener.onAddContactClicked()
    override fun onCreateGroupClicked() = listener.onCreateGroupClicked()
}
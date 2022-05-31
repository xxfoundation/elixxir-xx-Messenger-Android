package io.xxlabs.messenger.ui.main.chats

class ChatsList(
    private val listener: ChatsListListener,
    override val dividerVisible: Boolean = false
) : ChatsListUI {
    override fun onAddContactClicked() = listener.onAddContactClicked()
    override fun onCreateGroupClicked() = listener.onCreateGroupClicked()
}
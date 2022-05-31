package io.xxlabs.messenger.ui.main.chats

import io.xxlabs.messenger.data.room.model.Contact

class NewConnection(
    private val listener: NewConnectionListener,
    override val contact: Contact
) : NewConnectionUI {
    override fun onNewConnectionClicked(contact: Contact) = listener.onNewConnectionClicked(contact)
}
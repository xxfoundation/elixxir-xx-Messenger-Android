package io.xxlabs.messenger.ui.main.chats

import io.xxlabs.messenger.data.room.model.Contact

interface NewConnectionListener {
    fun onNewConnectionClicked(contact: Contact)
}
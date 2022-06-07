package io.xxlabs.messenger.ui.main.chats.newConnections

import io.xxlabs.messenger.data.room.model.ContactData

interface NewConnectionListener {
    fun onNewConnectionClicked(contact: ContactData)
}
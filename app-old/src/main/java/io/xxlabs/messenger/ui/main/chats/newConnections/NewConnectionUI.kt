package io.xxlabs.messenger.ui.main.chats.newConnections

import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail

interface NewConnectionUI : ItemThumbnail {
    val contact: ContactData
    fun onNewConnectionClicked(contact: ContactData)
}
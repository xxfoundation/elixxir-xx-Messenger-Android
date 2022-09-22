package io.xxlabs.messenger.ui.main.chats.search

import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.Group

interface SearchResultListener {
    fun onConnectionClicked(contact: Contact)
    fun onGroupChatClicked(group: Group)
    fun onAddContactClicked()
}
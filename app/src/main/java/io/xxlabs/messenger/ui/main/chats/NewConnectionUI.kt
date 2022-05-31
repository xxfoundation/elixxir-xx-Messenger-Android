package io.xxlabs.messenger.ui.main.chats

import io.xxlabs.messenger.data.room.model.Contact


interface NewConnectionUI {
    val contact: Contact
    fun onContactClicked(contact: Contact)
}
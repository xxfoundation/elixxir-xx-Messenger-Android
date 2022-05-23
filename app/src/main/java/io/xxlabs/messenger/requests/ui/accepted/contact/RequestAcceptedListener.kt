package io.xxlabs.messenger.requests.ui.accepted.contact

import io.xxlabs.messenger.data.room.model.Contact

interface RequestAcceptedListener {
    fun sendMessage(contact: Contact)
}
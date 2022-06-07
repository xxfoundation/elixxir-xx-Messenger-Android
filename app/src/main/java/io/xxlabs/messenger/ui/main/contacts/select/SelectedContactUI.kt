package io.xxlabs.messenger.ui.main.contacts.select

import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.ui.main.contacts.list.ContactItem

interface SelectedContactUI {
    val contact: Contact
    val thumbnail: ItemThumbnail
    fun onRemoveClicked()

    override fun equals(other: Any?): Boolean
}

interface SelectedContactListener {
    fun onContactRemoved(selection: SelectedContact)
}

data class SelectedContact(
    val contactItem: ContactItem,
    private val listener: SelectedContactListener,
    override val thumbnail: ItemThumbnail,
) : SelectedContactUI {
    override val contact: Contact = contactItem.model

    override fun onRemoveClicked() = listener.onContactRemoved(this)
}
package io.xxlabs.messenger.ui.main.contacts.select

import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail

interface SelectedContactUI {
    val contact: Contact
    val thumbnail: ItemThumbnail
    fun onRemoveClicked()

    override fun equals(other: Any?): Boolean
}

interface SelectedContactListener {
    fun onContactRemoved(contact: SelectedContact)
}

data class SelectedContact(
    private val listener: SelectedContactListener,
    override val thumbnail: ItemThumbnail,
    override val contact: Contact
) : SelectedContactUI {
    override fun onRemoveClicked() = listener.onContactRemoved(this)
}
package io.xxlabs.messenger.ui.main.contacts.list

import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.data.room.model.dummyThumbnail
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.support.dummy.randomString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed interface Connection {
    val listener: ConnectionListener
    val thumbnail: ItemThumbnail
    val name: String

    fun onClick() = listener.onClicked(this)
}

data class ContactItem(
    val model: Contact,
    override val listener: ConnectionListener,
    override val thumbnail: ItemThumbnail,
    override val name: String = model.displayName,
) : Connection

data class GroupItem(
    val model: Group,
    override val listener: ConnectionListener,
    override val thumbnail: ItemThumbnail,
    override val name: String = model.name,
) : Connection

data class SelectableContact(
    val contactItem: ContactItem,
    override val listener: ConnectionListener,
    val selected: Boolean,
) : Connection {
    val model: Contact = contactItem.model
    override val thumbnail: ItemThumbnail = contactItem.thumbnail
    override val name: String = model.displayName
}

fun dummyContacts(listener: ConnectionListener? = null, count: Int = 20): List<ContactItem> {
    val dummyList = mutableListOf<ContactItem>()
    repeat(count) {
        dummyList.add(dummyContactItem(listener,10) )
    }
    return dummyList.sortedBy { it.name.lowercase() }
}

fun createDummyContactsFlow(listener: ConnectionListener? = null): Flow<List<ContactItem>> = flow {
    emit(dummyContacts(listener))
}

private fun dummyContactItem(listener: ConnectionListener? = null, maxNameLength: Int): ContactItem {
    val dummyContact = ContactData(nickname = randomString(maxNameLength))
    return ContactItem(
        dummyContact,
        listener ?: dummyListener,
        dummyContact.dummyThumbnail()
    )
}

private val dummyListener = object: ConnectionListener {
    override fun onClicked(connection: Connection) {}
}

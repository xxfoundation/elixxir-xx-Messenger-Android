package io.xxlabs.messenger.ui.main.contacts.list

import io.xxlabs.messenger.data.room.model.*
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
) : Connection by contactItem

fun dummyContacts(count: Int = 20): List<ContactItem> {
    val dummyList = mutableListOf<ContactItem>()
    repeat(count) {
        dummyList.add(dummyContactItem(10) )
    }
    return dummyList.sortedBy { it.name.lowercase() }
}

val dummyContactsFlow: Flow<List<ContactItem>> = flow {
    emit(dummyContacts())
}

private fun dummyContactItem(maxNameLength: Int): ContactItem {
    val dummyContact = ContactData(nickname = randomString(maxNameLength))
    return ContactItem(
        dummyContact,
        dummyListener,
        dummyContact.dummyThumbnail()
    )
}

private val dummyListener = object: ConnectionListener {
    override fun onClicked(connection: Connection) {}
}

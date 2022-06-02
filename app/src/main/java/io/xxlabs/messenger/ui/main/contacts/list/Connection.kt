package io.xxlabs.messenger.ui.main.contacts.list

import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.support.dummy.randomString

sealed class Connection {
    abstract val listener: ConnectionListener
    abstract val thumbnail: ItemThumbnail
    abstract val name: String

    open fun onClick() = listener.onClicked(this)
}

data class ContactItem(
    val model: Contact,
    override val listener: ConnectionListener,
    override val thumbnail: ItemThumbnail,
    override val name: String = model.displayName,
): Connection()

data class GroupItem(
    val model: Group,
    override val listener: ConnectionListener,
    override val thumbnail: ItemThumbnail,
    override val name: String = model.name,
): Connection()


fun dummyContacts(count: Int = 20): List<ContactItem> {
    val dummyList = mutableListOf<ContactItem>()
    repeat(count) {
        dummyList.add(dummyContactItem(10) )
    }
    return dummyList
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

package io.xxlabs.messenger.ui.main.contacts

import io.xxlabs.messenger.data.room.model.ContactData

interface GroupsSelectionListener {
    fun onAddMember(contact: ContactData)
    fun onRemoveMember(viewHolder: ContactsViewHolder?, contact: ContactData, pos: Int)
    fun onRemoveMember(contact: ContactData, pos: Int)
}
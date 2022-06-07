package io.xxlabs.messenger.ui.main.contacts.deprecated

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.extensions.toBase64String
import timber.log.Timber

class GroupMembersListAdapter(private val groupsSelectionListener: GroupsSelectionListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var membersList: MutableList<ContactData> = mutableListOf()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_item_member, parent, false)
        return GroupMemberViewHolder.newInstance(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.context = recyclerView.context
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as GroupMemberViewHolder
        val contact = membersList[position]
        bindContact(contact, holder, position)

        holder.itemView.contentDescription = "contacts.group.members.item.$position"
    }

    private fun bindContact(
        contact: ContactData,
        holder: GroupMemberViewHolder,
        position: Int
    ) {
        val displayName = contact.displayName
        val initials = contact.initials
        Timber.v("Contact Loader %s", contact.userId.toBase64String())

        holder.setContactId(contact.id, contact.userId)
        holder.setContactUsernameText(displayName)
        holder.setPhoto(contact.photo, initials)
        holder.contactCancelBtn.setOnSingleClickListener {
            groupsSelectionListener.onRemoveMember(contact, position)
        }
    }

    override fun getItemCount(): Int {
        return membersList.size
    }

    fun addMember(contact: ContactData) {
        membersList.add(contact)
        notifyItemInserted(membersList.size - 1)
    }

    fun removeMember(contact: ContactData) {
        val index = membersList.indexOf(contact)
        if (index != -1) {
            membersList.remove(contact)
            notifyItemRemoved(index)
        }
    }

    fun removeAll() {
        val currSize = membersList.size
        membersList.clear()
        notifyItemRangeChanged(0, currSize)
    }

    fun getGroup(): List<ContactData> {
        return membersList
    }
}
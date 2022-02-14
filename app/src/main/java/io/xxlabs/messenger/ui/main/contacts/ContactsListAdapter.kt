package io.xxlabs.messenger.ui.main.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.SelectionMode
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.ui.main.chat.viewholders.HeaderViewHolder
import timber.log.Timber
import java.util.*

class ContactsListAdapter(
    private val isSelectionMode: Boolean = false,
    private val groupsSelectionListener: GroupsSelectionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var composedItemsList: MutableList<Any> = mutableListOf()
    private var itemsList: MutableList<Any> = mutableListOf()
    private var headerPositions: MutableList<Int> = mutableListOf()
    var isSearching = false
    var chooseMode = false

    private lateinit var filteredContactsList: MutableList<*>
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView

    override fun getItemViewType(position: Int): Int {
        return when {
            composedItemsList[position] is ContactData
                    || composedItemsList[position] is GroupData
                    || isSearching -> {
                CONTACT_OR_GROUP_VIEW
            }
            else -> {
                HEADER_VIEW
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context

        return if (viewType == CONTACT_OR_GROUP_VIEW) {
            val view =
                LayoutInflater.from(context)
                    .inflate(R.layout.list_item_contact, parent, false)
            ContactsViewHolder.newInstance(view)
        } else {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_contact_header, parent, false)
            HeaderViewHolder.newInstance(view)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        this.context = recyclerView.context
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isSearching) {
            holder as ContactsViewHolder
            if (filteredContactsList[position] is ContactData) {
                val contact = filteredContactsList[position] as ContactData
                bindContact(contact, holder, position)
                holder.setSetChooseMode(chooseMode)
                holder.setChooseModeListener(contact, groupsSelectionListener)
            } else {
                bindGroup(filteredContactsList[position] as GroupData, holder, position)
                holder.setChooseModeGroups(filteredContactsList[position] as GroupData)
            }
        } else {
            if (holder is HeaderViewHolder) {
                holder.bindTo(composedItemsList[position] as String)
            } else {
                val item = composedItemsList[position]
                if (item is ContactData) {
                    holder as ContactsViewHolder
                    bindContact(item, holder, position)
                    holder.setSetChooseMode(chooseMode)
                    holder.setChooseModeListener(item, groupsSelectionListener)
                } else {
                    item as GroupData
                    holder as ContactsViewHolder
                    bindGroup(item, holder, position)
                    holder.setChooseModeGroups(item)
                }
            }
        }

        holder.itemView.contentDescription = "contacts.list.item.$position"
    }

    private fun bindContact(
        contact: ContactData,
        holder: ContactsViewHolder,
        position: Int
    ) {
        val displayName = contact.displayName
        val initials = contact.initials
        Timber.v("Contact Loader %s", contact.userId.toBase64String())

        holder.setContactId(contact.id, contact.userId)
        holder.setContactStatus(contact.status)
        holder.setContactUsernameText(displayName)
        holder.setState(contact.status)
        holder.setPhoto(contact.photo, initials)

        if (isSelectionMode) {
            holder.setOnClick(SelectionMode.CONTACT_ACCESS)
        } else {
            holder.setOnClick(SelectionMode.PROFILE_ACCESS)
        }

        holder.showDivider(position != 0 && position != composedItemsList.size - 1)
    }

    private fun bindGroup(
        group: GroupData,
        holder: ContactsViewHolder,
        position: Int
    ) {
        val displayName = group.name
        val initials = group.name.substring(0, 2).uppercase()
        Timber.v("Group Loader %s", group.groupId.toBase64String())

        holder.setContactId(group.id, group.groupId)
        holder.setContactUsernameText(displayName)
        holder.setPhoto(null, initials)

        holder.setOnClick(SelectionMode.GROUP_ACCESS)
        holder.showDivider(position != 0 && position != composedItemsList.size - 1)
    }

    private fun showHeader(position: Int): Boolean {
        if (position == 0) {
            return true
        }
        val previousPos = itemsList[position - 1]
        val currPos = itemsList[position]

        val contactName = when (previousPos) {
            is ContactData -> {
                previousPos.displayName
            }
            is GroupData -> {
                previousPos.name
            }
            else -> {
                ""
            }
        }

        val anotherContactName = when (currPos) {
            is ContactData -> {
                currPos.displayName
            }
            is GroupData -> {
                currPos.name
            }
            else -> {
                ""
            }
        }

        return contactName.lowercase(Locale.getDefault())[0].compareTo(
            anotherContactName.lowercase(Locale.getDefault())[0]
        ) != 0
    }

    override fun getItemCount(): Int {
        return if (isSearching) {
            if (this::filteredContactsList.isInitialized) {
                filteredContactsList.size
            } else {
                0
            }
        } else {
            composedItemsList.size
        }
    }

    fun update(newData: List<Any>) {
        itemsList = mutableListOf()
        itemsList = newData.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER, { item ->
                if (item is ContactData) {
                    item.displayName
                } else {
                    item as GroupData
                    item.name
                }
            })
        ).toMutableList()
        createComposedList(itemsList)
        notifyItemRangeChanged(0, itemsList.size)
    }

    private fun createComposedList(itemList: MutableList<Any>) {
        composedItemsList = mutableListOf()
        headerPositions = mutableListOf()

        itemList.forEachIndexed { index, contact ->
            if (showHeader(index)) {
                val name = when (contact) {
                    is ContactData -> {
                        contact.displayName
                    }
                    is GroupData -> {
                        contact.name
                    }
                    else -> {
                        ""
                    }
                }
                composedItemsList.add(name)
                headerPositions.add(composedItemsList.size - 1)
                composedItemsList.add(contact)
            } else {
                composedItemsList.add(contact)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filteredResults: MutableList<Any> = if (charString.isEmpty()) {
                    itemsList.toMutableList()
                } else {
                    val filteredList: MutableList<Any> = mutableListOf()
                    for (row in itemsList) {
                        if (row is ContactData) {
                            if (containsNickname(row.nickname, charString)
                                || containsUsername(row.username, charString)
                            ) {
                                filteredList.add(row)
                            }
                        } else {
                            row as GroupData
                            if (row.name.contains(charString, true)) {
                                filteredList.add(row)
                            }
                        }
                    }

                    filteredList.sortWith { first, second ->
                        ContactData.compare(first, second)
                    }

                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredResults
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                filteredContactsList = filterResults.values as MutableList<*>
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    private fun containsUsername(username: String?, charString: String): Boolean {
        return username?.contains(charString, true) == true
    }

    private fun containsNickname(nickname: String?, charString: String): Boolean {
        return if (nickname.isNullOrEmpty()) {
            false
        } else {
            nickname.contains(charString, true)
        }
    }

    fun defineChooseMode(enabled: Boolean) {
        chooseMode = enabled
        notifyItemRangeChanged(0, itemCount)
    }

    fun findPositionOf(contact: ContactData): Int {
        return composedItemsList.indexOf(contact)
    }

    fun deselectAll() {
        composedItemsList.forEach { item ->
            if (item is ContactData) {
                val pos = findPositionOf(item)
                (recyclerView.findViewHolderForAdapterPosition(pos) as ContactsViewHolder?)
                    ?.deselectContact()
            }
        }

        notifyItemRangeChanged(0, itemCount)
    }

    companion object {
        const val HEADER_VIEW = 0
        const val CONTACT_OR_GROUP_VIEW = 1
    }
}
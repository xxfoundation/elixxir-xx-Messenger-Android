package io.xxlabs.messenger.ui.main.chats

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.ChatWrapper
import io.xxlabs.messenger.data.datatype.SelectionMode
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.selection.CustomSelectionTracker
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.main.contacts.ContactsViewHolder
import timber.log.Timber
import java.util.*

class ChatsListAdapter(
    val daoRepo: DaoRepository,
    val schedulers: SchedulerProvider
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var subscriptions = CompositeDisposable()
    var chats: MutableList<ChatWrapper> = mutableListOf()
    var chatsFiltered: MutableList<*> = mutableListOf<Any>()

    var tracker: CustomSelectionTracker? = null
    var selectionMode: SelectionMode = SelectionMode.CHAT_ACCESS

    private var lastSearch = ""
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView

    override fun getItemId(position: Int): Long {
        return when {
            position >= itemCount -> {
                RecyclerView.NO_POSITION.toLong()
            }
            chatsFiltered[position] is ChatWrapper -> {
                (chatsFiltered[position] as ChatWrapper).getItemId()
            }
            chatsFiltered[position] is ContactData -> {
                (chatsFiltered[position] as ContactData).id
            }
            chatsFiltered[position] is GroupData -> {
                sumGroupId((chatsFiltered[position] as GroupData).id)
            }

            else -> {
                RecyclerView.NO_POSITION.toLong()
            }
        }
    }

    private fun sumGroupId(id: Long): Long {
        return id + 10000
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatsFiltered[position] is ChatWrapper) {
            0
        } else {
            1
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        this.context = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        return if (viewType == 0) {
            ChatsViewHolder.create(parent)
        } else {
            val view = LayoutInflater.from(context).inflate(
                R.layout.list_item_chat_contact, parent,
                false
            )
            ContactsViewHolder.newInstance(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatsViewHolder) {
            val item = chatsFiltered[position] as ChatWrapper
            bindChatDetails(holder, item)
        } else {
            val contactHolder = holder as ContactsViewHolder
            if (chatsFiltered[position] is ContactData) {
                val item = chatsFiltered[position] as ContactData
                bindContact(item, contactHolder)
                tracker?.let {
                    holder.isSelected(it.isSelected(item.id))
                }
            }
        }

        holder.itemView.contentDescription = "chats.list.item.$position"
    }

    private fun bindChatDetails(holder: ChatsViewHolder, chat: ChatWrapper) {
        tracker?.let { holder.isSelected(it.isSelected(chat.getItemId())) }
        Timber.v("Fetch Contact id: ${chat.getItemBindingsId().toBase64String()}")
        Timber.v("Fetch Message id: ${chat.getLastMessageId()}")
        Timber.v("Fetch Unread count: ${chat.unreadCount}")
        if (chat.item is GroupData) {
            val group = chat.item
            val lastMessage = chat.lastMessage as GroupMessageData?
            holder.setGroup(group)
            bindLastMessage(holder, lastMessage)
        } else {
            val contact = chat.item as ContactData
            val lastMessage = chat.lastMessage as PrivateMessageData?
            holder.setContact(contact)
            bindLastMessage(holder, lastMessage)
        }

        holder.setSelectionOptions(selectionMode)
        holder.setBold(false)
        holder.setUnreadCount(chat.unreadCount, chat.isLastSenderContact())
    }

    private fun bindLastMessage(
        holder: ChatsViewHolder,
        lastMessage: ChatMessage?
    ) {
        if (lastMessage == null) {
            holder.setPreview("")
            holder.setDateText("")
        } else {
            val date = Date(lastMessage.timestamp)
            val cal = Calendar.getInstance()
            cal.time = date

            val dateText =
                if (Utils.isToday(cal)) Utils.getTimestampString(date.time)
                else Utils.calculateGetTimestampString(date.time, "MM/dd/yyyy")

            holder.setDateText(dateText)

            val text = lastMessage.payloadWrapper.text

            if (lastSearch.isNotEmpty()) holder.setPreviewHighlight(text, lastSearch)
            else holder.setPreview(text)

            if (lastMessage.unread) holder.setBold(true)
            else holder.setBold(false)
        }
    }

    private fun bindContact(
        contact: ContactData,
        holder: ContactsViewHolder
    ) {
        val username = contact.displayName
        Timber.v("Contact Loader %s", contact.displayName)
        holder.setContactId(contact.id, contact.userId)
        holder.setContactUsernameText(username)
        holder.setState(contact.status)
        holder.setPhoto(contact.photo, contact.initials)
        holder.showDivider(false)
        holder.setOnClick(selectionMode)
    }

    override fun getItemCount(): Int {
        return chatsFiltered.size
    }

    fun update(updatedChats: List<ChatWrapper>) {
        Timber.v("Chats size: ${updatedChats.size}")
        chats.clear()
        chatsFiltered.clear()
        chats.addAll(updatedChats)
        chatsFiltered = chats
        Timber.v("Chats filtered: ${chatsFiltered.size}")
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        subscriptions.clear()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val searchString = charSequence.toString()
                lastSearch = searchString
                val filteredResults: MutableList<*>
                if (searchString.isEmpty()) {
                    filteredResults = chats.toMutableList()
                } else {
                    val filteredList: MutableList<Any> = mutableListOf()
                    for (row in chats) {
                        val contact = row.item
                        val lastMessage = row.lastMessage

                        if (containsLastMessage(lastMessage, searchString) ||
                            containsContactOrGroup(contact, searchString)
                        ) {
                            filteredList.add(row)
                        }
                    }

                    filteredList.sortWith { first, second ->
                        compareResults(first, second)
                    }

                    filteredResults = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = filteredResults
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                chatsFiltered = filterResults.values as ArrayList<*>
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    private fun compareResults(first: Any?, second: Any?): Int {
        return if (first is ChatWrapper && second is ChatWrapper) {
            0
        } else if (first is ChatWrapper && second is ContactData) {
            1
        } else if (first is ContactData && second is ChatWrapper) {
            -1
        } else if (first is ContactData && second is ContactData) {
            ContactData.compare(first, second)
        } else {
            0
        }
    }

    private fun containsLastMessage(lastMessage: ChatMessage?, charString: String): Boolean =
        lastMessage?.payloadWrapper?.text?.contains(charString, true)
            ?: false

    private fun containsContactOrGroup(item: Any?, charSequence: String): Boolean {
        return when (item) {
            null -> {
                false
            }
            is ContactData -> {
                (item.username.contains(charSequence, true) || item.nickname.contains(
                    charSequence,
                    true
                ))
            }
            else -> {
                item as GroupData
                (item.name.contains(charSequence, true))
            }
        }
    }

    fun deselectChats(deselectList: List<ChatWrapper>) {
        deselectList.forEach { chat ->
            val index = chatsFiltered.indexOf(chat)
            if (index != -1) {

            }
        }
    }

    fun getChats(idsListFromTracker: List<Long>): List<ChatWrapper> {
        return chats.filter { chat ->
            idsListFromTracker.contains(chat.getItemId())
        }
    }
}
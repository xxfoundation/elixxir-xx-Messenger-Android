package io.xxlabs.messenger.ui.main.chats

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.SelectionMode
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.databinding.ListItemChatBinding
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.support.view.BitmapResolver
import timber.log.Timber
import java.util.regex.Pattern

class ChatsViewHolder(
    private val binding: ListItemChatBinding
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    private val defaultItemThumbnail: ItemThumbnail by lazy {
        object : ItemThumbnail {
            override val itemPhoto: Bitmap? = null
            override val itemIconRes: Int = R.drawable.ic_contact_light
            override val itemInitials: String? = null
        }
    }

    var currentObj: Any? = null
    var bindingsId: ByteArray = byteArrayOf()
    var rowId: Long = -1
    private var selectionMode = SelectionMode.CHAT_ACCESS

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (selectionMode == SelectionMode.CHAT_ACCESS) {
            if (currentObj is GroupData) {
                val group = currentObj as GroupData?
                Timber.v("recipient ID BEING USED TO OPEN ${group?.groupId ?: bindingsId}")
                val bundle = bundleOf(
                    "group_id" to (group?.groupId ?: bindingsId)
                )

                Navigation.findNavController(itemView.context as Activity, R.id.mainNavHost)
                    .navigate(R.id.action_global_groups_chat, bundle)
            } else if (currentObj is ContactData) {
                val contact = currentObj as ContactData?
                Timber.v("recipient ID BEING USED TO OPEN ${contact?.userId ?: bindingsId}")
                val bundle = bundleOf(
                    "contact_id" to (contact?.userId ?: bindingsId)
                )

                Navigation.findNavController(itemView.context as Activity, R.id.mainNavHost)
                    .navigate(R.id.action_global_chat, bundle)
            }
        }
    }

    /**
     * @param text
     */
    fun setDateText(text: String) {
        binding.chatTimestamp.apply {
            try {
                this.text = text
            } catch (err: Exception) {
                err.localizedMessage
                visibility = View.GONE
            }
        }
    }

    /**
     * @param text
     */
    fun setPreview(text: String) {
        binding.chatMsgPreview.text = text
    }

    fun setPreviewHighlight(text: String, lastSearch: String) {
        val stringBuilder = SpannableStringBuilder(text)
        val p = Pattern.compile(lastSearch, Pattern.CASE_INSENSITIVE or Pattern.LITERAL)
        val m = p.matcher(text)
        while (m.find()) {
            stringBuilder.setSpan(
                BackgroundColorSpan(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.yellowLightTheme
                    )
                ),
                m.start(),
                m.end(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

        binding.chatMsgPreview.text = stringBuilder
    }

    fun setBold(isNotViewed: Boolean) {
        if (isNotViewed) {
            binding.chatTimestamp.setTypeface(null, Typeface.BOLD)
            binding.chatUsername.setTypeface(null, Typeface.BOLD)
            binding.chatMsgPreview.setTypeface(null, Typeface.BOLD)
        } else {
            binding.chatTimestamp.setTypeface(null, Typeface.NORMAL)
            binding.chatUsername.setTypeface(null, Typeface.NORMAL)
            binding.chatMsgPreview.setTypeface(null, Typeface.NORMAL)
        }
    }

    fun setContact(newContact: ContactData) {
        currentObj = newContact
        rowId = newContact.id
        bindingsId = newContact.userId
        binding.chatUsername.text = newContact.displayName
        setPhoto(newContact)
    }

    fun setGroup(newGroup: GroupData) {
        currentObj = newGroup
        rowId = newGroup.id + 10000
        bindingsId = newGroup.groupId
        binding.chatUsername.text = newGroup.name

        setThumbnail(groupData = newGroup)
    }

    private fun setThumbnail(contactData: ContactData? = null, groupData: GroupData? = null) {
        val ui = contactData?.let { contact ->
            object : ItemThumbnail {
                override val itemPhoto: Bitmap? = resolveBitmap(contact.photo)
                override val itemIconRes: Int? = null
                override val itemInitials: String = contact.initials

            }
        } ?: groupData?.let {
            object : ItemThumbnail {
                override val itemPhoto: Bitmap? = null
                override val itemIconRes: Int = R.drawable.ic_group_chat
                override val itemInitials: String? = null
            }
        } ?: defaultItemThumbnail
        binding.ui = ui
    }

    private fun resolveBitmap(data: ByteArray?): Bitmap? {
        return data?.let {
            BitmapResolver.getBitmap(data)
        }
    }

    private fun setPhoto(contact: ContactData) {
        setThumbnail(contact)
    }

    fun setUnreadCount(unreadCount: Int, isLastSenderContact: Boolean) {
        binding.chatUnreadCount.findViewById<TextView>(R.id.chatUnreadCount)?.apply {
            if (isLastSenderContact && unreadCount > 0) {
                visibility = View.VISIBLE
                text = unreadCount.toString()
            } else {
                visibility = View.GONE
            }
        }
    }

    fun isSelected(isChecked: Boolean = false) {
        binding.chatCheckbox.isChecked = isChecked
    }

    fun setSelectionOptions(selectionMode: SelectionMode) {
        if (selectionMode == SelectionMode.CHAT_SELECTION) {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.neutral_off_white
                )
            )
            binding.chatCheckbox.visibility = View.VISIBLE
        } else {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.transparent
                )
            )
            binding.chatCheckbox.visibility = View.GONE
        }
        this.selectionMode = selectionMode
    }

    companion object {
        fun create(parent: ViewGroup): ChatsViewHolder {
            val binding = ListItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ChatsViewHolder(binding)
        }
    }
}
package io.xxlabs.messenger.ui.main.groups

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.GroupMessage
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.ui.main.chat.adapters.ChatMessageDiffCallback
import io.xxlabs.messenger.ui.main.chat.adapters.ChatMessagesAdapter
import java.util.*

class GroupMessagesAdapter(
    private val chatViewModel: GroupMessagesViewModel
) : ChatMessagesAdapter<GroupMessage, GroupMessageViewHolder>(GroupMessageDiffCallback()) {

    private var groupMembers = UserIdToUsernameMap(hashMapOf())
    private var listener = GroupMessageItemListener(chatViewModel)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getMessageId(position)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupMessageViewHolder {
        return when (viewType) {
            R.layout.list_item_msg_sent -> SentMessageViewHolder.create(parent)
            else -> ReceivedMessageViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(
        holder: GroupMessageViewHolder,
        position: Int
    ) {
        val header = showHeader(position)
        getItem(position)?.let {

            holder.onBind(it, listener, header, selectionMode, chatViewModel)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position) ?: return RecyclerView.NO_POSITION

        return when (message.status) {
            MessageStatus.RECEIVED.value -> R.layout.list_item_msg_received
            else -> R.layout.list_item_msg_sent
        }
    }

    fun updateGroupMembers(members: UserIdToUsernameMap) {
        groupMembers = members
        notifyDataSetChanged()
    }

    /**
     * Passes list item events to the ChatViewModel.
     */
    inner class GroupMessageItemListener(
        private val chatViewModel: GroupMessagesViewModel
    ) : GroupMessageListener<GroupMessage> {
        override fun onClick(message: GroupMessage) =
            chatViewModel.onMessageClicked(message)

        override fun onLongClick(message: GroupMessage) =
            chatViewModel.onMessageLongClicked(message)

        override fun onReplyClicked(reply: ReplyWrapper) =
            chatViewModel.onReplyPreviewClicked(reply.uniqueId)

        override val membersList: UserIdToUsernameMap
            get() = this@GroupMessagesAdapter.groupMembers

        override fun lookupMessage(uniqueId: ByteArray): GroupMessage? =
            chatViewModel.lookupMessage(uniqueId)

        override fun onShowMixClicked(message: GroupMessage) {
            chatViewModel.onShowMixClicked(message)
        }

        override fun onImageClicked(imageUri: String) { }
    }
}

/**
 * Handles diff calculations as the message list changes.
 */
class GroupMessageDiffCallback : ChatMessageDiffCallback<GroupMessage>() {
    override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean =
        oldItem == newItem
}
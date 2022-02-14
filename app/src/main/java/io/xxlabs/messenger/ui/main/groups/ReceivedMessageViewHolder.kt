package io.xxlabs.messenger.ui.main.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.xxlabs.messenger.data.room.model.GroupMessage
import io.xxlabs.messenger.databinding.ListItemMsgReceivedBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener

class ReceivedMessageViewHolder(
    private val binding: ListItemMsgReceivedBinding
) : GroupMessageViewHolder(binding.root) {

    override val rootLayout = binding.itemMsgReceivedLayout
    override val checkbox = binding.itemMsgReceivedCheckbox
    override val msgTextView = binding.itemMsgReceived
    override val timeStampText = binding.itemMsgReceivedTimestamp
    override val urlPreviewLayout = binding.itemMsgReceivedUrlLayout
    override val replyLayout = binding.itemMsgReceivedReplyLayout
    override val replyIconMsg = binding.itemMsgReceivedReplyIconMsg
    override val replyToUsername = binding.itemMsgReceivedReplyUsername
    override val replyTextView = binding.itemMsgReceivedReplyText
    override val replyIcon = binding.itemMsgReceivedReplyIconMsg

    override fun onBind(
        message: GroupMessage,
        listener: MessageListener<GroupMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<GroupMessage>,
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)

        binding.message = message
        binding.listener = listener
        binding.itemMsgReceivedUsername.text = username

        binding.itemMsgReceivedShowMix.visibility =
            if (message.roundUrl.isNullOrEmpty()) View.GONE
            else View.VISIBLE
    }

    companion object {
        fun create(parent: ViewGroup): GroupMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgReceivedBinding = ListItemMsgReceivedBinding.inflate(
                layoutInflater, parent, false
            )

            return ReceivedMessageViewHolder(binding)
        }
    }
}
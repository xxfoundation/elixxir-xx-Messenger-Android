package io.xxlabs.messenger.ui.main.chat.viewholders.text

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.databinding.ListItemMsgReceivedBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder

open class ReceivedMessageViewHolder<T: ChatMessage>(
    private val binding: ListItemMsgReceivedBinding,
) : MessageViewHolder<T>(binding.root) {

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

    init {
        binding.itemMsgReceivedUsername.visibility = View.GONE
    }

    override fun onBind(
        message: T,
        listener: MessageListener<T>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<T>,
    ) {
        binding.message = message
        binding.listener = listener

        binding.itemMsgReceivedShowMix.visibility =
            if (message.roundUrl.isNullOrEmpty()) View.GONE
            else View.VISIBLE

        super.onBind(message, listener, header, selectionMode, chatViewModel)
    }

    companion object {
        fun create(parent: ViewGroup): MessageViewHolder<PrivateMessage> {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgReceivedBinding = ListItemMsgReceivedBinding.inflate(
                layoutInflater, parent, false
            )

            return ReceivedMessageViewHolder(binding)
        }
    }
}
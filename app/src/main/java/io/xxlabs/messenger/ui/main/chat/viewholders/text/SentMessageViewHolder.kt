package io.xxlabs.messenger.ui.main.chat.viewholders.text

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.databinding.ListItemMsgSentBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder

open class SentMessageViewHolder<T: ChatMessage>(
    private val binding: ListItemMsgSentBinding
) : MessageViewHolder<T>(binding.root) {

    override val rootLayout = binding.itemMsgSentLayout
    override val checkbox = binding.itemMsgSentCheckbox
    override val msgTextView = binding.itemMsgSent
    override val timeStampText = binding.itemMsgSentTimestamp
    override val urlPreviewLayout = binding.itemMsgSentUrlLayout
    override val replyLayout = binding.itemMsgSentReplyLayout
    override val replyIconMsg = binding.itemMsgSentReplyIconMsg
    override val replyToUsername = binding.itemMsgSentReplyUsername
    override val replyTextView = binding.itemMsgSentReplyText
    override val replyIcon = binding.itemMsgSentReplyIconMsg
    private val sentIcon = binding.itemMsgSentIcon
    private val errorText = binding.itemMsgSentErrorText

    override fun onBind(
        message: T,
        listener: MessageListener<T>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<T>,
    ) {
        binding.message = message
        binding.listener = listener

        setSentStatus(MessageStatus.fromInt(message.status))
        super.onBind(message, listener, header, selectionMode, chatViewModel)
    }

    override fun updateAccessibility() {
        super.updateAccessibility()
        errorText.contentDescription = "chat.item.$absoluteAdapterPosition.error"
    }

    private fun setSentStatus(status: MessageStatus) {
        sentIcon.setImageResource(R.drawable.ic_lock_white)

        when (status) {
            MessageStatus.FAILED -> {
                rootLayout.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.chatBgColorError)
                errorText.visibility = View.VISIBLE
                sentIcon.setImageResource(R.drawable.ic_send_failure)
                binding.itemMsgSentShowMix.visibility = View.GONE
            }
            else -> {
                rootLayout.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.brand_dark)
                errorText.visibility = View.GONE
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): MessageViewHolder<PrivateMessage> {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgSentBinding = ListItemMsgSentBinding.inflate(
                layoutInflater, parent, false
            )

            return SentMessageViewHolder(binding)
        }
    }
}
package io.xxlabs.messenger.ui.main.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.GroupMessage
import io.xxlabs.messenger.databinding.ListItemMsgSentBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener

class SentMessageViewHolder(
    private val binding: ListItemMsgSentBinding
) : GroupMessageViewHolder(binding.root) {

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
        message: GroupMessage,
        listener: MessageListener<GroupMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<GroupMessage>,
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)

        binding.message = message
        binding.listener = listener

        setSentStatus(MessageStatus.fromInt(message.status))
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
        fun create(parent: ViewGroup): GroupMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgSentBinding = ListItemMsgSentBinding.inflate(
                layoutInflater, parent, false
            )

            return SentMessageViewHolder(binding)
        }
    }
}
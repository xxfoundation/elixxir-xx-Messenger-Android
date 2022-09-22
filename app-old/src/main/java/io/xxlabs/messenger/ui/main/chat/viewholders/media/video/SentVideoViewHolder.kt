package io.xxlabs.messenger.ui.main.chat.viewholders.media.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.databinding.ListItemMsgVideoSentBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener

class SentVideoViewHolder(
    private val binding: ListItemMsgVideoSentBinding
): VideoViewHolder(binding.root){

    /* MessageViewHolder */

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

    /* VideoViewHolder */

    override val videoPlayer = binding.videoPlayer

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>,
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
            }
            else -> {
                rootLayout.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.brand_dark)
                errorText.visibility = View.GONE
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup): SentVideoViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgVideoSentBinding = ListItemMsgVideoSentBinding.inflate(
                layoutInflater, parent, false
            )

            return SentVideoViewHolder(binding)
        }
    }
}
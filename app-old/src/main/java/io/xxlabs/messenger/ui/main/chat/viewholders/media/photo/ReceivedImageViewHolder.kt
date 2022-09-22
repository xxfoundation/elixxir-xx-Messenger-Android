package io.xxlabs.messenger.ui.main.chat.viewholders.media.photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.databinding.ListItemMsgPhotoReceivedBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener

class ReceivedImageViewHolder(
    private val binding: ListItemMsgPhotoReceivedBinding
) : ImageViewHolder(binding.root) {

    /* MessageViewHolder */

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

    /* ImageViewHolder */

    override val image = binding.photoContent

    init {
        binding.itemMsgReceivedUsername.visibility = View.GONE
    }

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>,
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)
        with (message.transferProgress.toInt()) {
            if (this < 99) binding.transferProgressBar.visibility = View.VISIBLE
            else binding.transferProgressBar.visibility = View.GONE
        }

        binding.message = message
        binding.listener = listener
    }

    companion object {
        fun create(parent: ViewGroup): ReceivedImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgPhotoReceivedBinding = ListItemMsgPhotoReceivedBinding.inflate(
                layoutInflater, parent, false
            )

            return ReceivedImageViewHolder(binding)
        }
    }
}
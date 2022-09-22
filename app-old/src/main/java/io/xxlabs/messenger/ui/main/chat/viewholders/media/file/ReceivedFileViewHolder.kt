package io.xxlabs.messenger.ui.main.chat.viewholders.media.file

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.databinding.ListItemMsgFileReceivedBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener

class ReceivedFileViewHolder(
    private val binding: ListItemMsgFileReceivedBinding
) : FileViewHolder(binding.root) {

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

    /* FileViewHolder */

    override val fileNameLabel = binding.fileNameLabel
    override val fileSizeLabel = binding.fileSizeLabel
    override val fileIcon = binding.fileIcon

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

        binding.message = message
        binding.listener = listener
    }

    companion object {
        fun create(parent: ViewGroup): ReceivedFileViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgFileReceivedBinding = ListItemMsgFileReceivedBinding.inflate(
                layoutInflater, parent, false
            )

            return ReceivedFileViewHolder(binding)
        }
    }
}
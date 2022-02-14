package io.xxlabs.messenger.ui.main.chat.viewholders.media.audio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.databinding.ListItemMsgAudioReceivedBinding
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.enabled
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener

class ReceivedAudioViewHolder(
    private val binding: ListItemMsgAudioReceivedBinding
) : AudioViewHolder(binding.root) {

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

    /* AudioViewHolder */

    override val audioPlayButton = binding.audioPlayButton
    override val audioPauseButton = binding.audioPauseButton
    override val audioSeekbar = binding.audioSeekbar
    override val audioMuteButton = binding.audioMuteButton
    override val audioUnmuteButton = binding.audioUnmuteButton
    override val audioTimeLabel = binding.audioTimeLabel

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
            if (this < 99) disablePlayback()
            else enablePlayback()
        }
        binding.message = message
        binding.listener = listener
    }

    private fun disablePlayback() {
        binding.audioLoadingLabel.visibility = View.VISIBLE
        binding.audioSeekbar.visibility = View.INVISIBLE
        binding.audioPlayButton.apply {
            alpha = 0.5F
            enabled(false)
        }
        binding.audioUnmuteButton.apply {
            alpha = 0.5F
            enabled(false)
        }
    }

    private fun enablePlayback() {
        binding.audioLoadingLabel.visibility = View.GONE
        binding.audioSeekbar.visibility = View.VISIBLE
        binding.audioPlayButton.apply {
            alpha = 1.0F
            enabled(true)
        }
        binding.audioUnmuteButton.apply {
            alpha = 1.0F
            enabled(true)
        }
    }

    companion object {
        fun create(parent: ViewGroup): ReceivedAudioViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding: ListItemMsgAudioReceivedBinding = ListItemMsgAudioReceivedBinding.inflate(
                layoutInflater, parent, false
            )

            return ReceivedAudioViewHolder(binding)
        }
    }
}
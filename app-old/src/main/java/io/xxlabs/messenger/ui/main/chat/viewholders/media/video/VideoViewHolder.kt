package io.xxlabs.messenger.ui.main.chat.viewholders.media.video

import android.net.Uri
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder

abstract class VideoViewHolder(parent: View): MessageViewHolder<PrivateMessage>(parent) {

    abstract val videoPlayer: VideoView

    private lateinit var message: PrivateMessage
    private var playerInitialized = false

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)
        this.message = message

        videoPlayer.apply {
            setOnClickListener {
                if (checkbox.visibility == View.VISIBLE) listener.onClick(message)
                else initMediaController()
            }
            setOnLongClickListener { listener.onLongClick(message); true }
        }
    }

    private fun VideoView.initMediaController() {
        val mediaController = MediaController(itemView.context)
        mediaController.setAnchorView(this)
        setMediaController(mediaController)
        setVideoURI(Uri.parse(message.fileUri))
        seekTo(1)
        setOnCompletionListener {
            stopPlayback()
        }
        setOnErrorListener { _, _, _ ->
            itemView.context.toast("Video not available.")
            true
        }

        playerInitialized = true
    }
}
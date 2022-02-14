package io.xxlabs.messenger.ui.main.chat.viewholders.media.file

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.fileSize
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder
import java.io.File

/**
 * Superclass for sent and received files.
 */
abstract class FileViewHolder(parent: View): MessageViewHolder<PrivateMessage>(parent) {

    abstract val fileNameLabel: TextView
    abstract val fileSizeLabel: TextView
    abstract val fileIcon: ImageView

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)

        fileNameLabel.setOnLongClickListener { listener.onLongClick(message); true }
        fileSizeLabel.setOnLongClickListener { listener.onLongClick(message); true }
        fileIcon.setOnLongClickListener { listener.onLongClick(message); true }

        message.fileUri?.let {
            val document = File(it)
            val fileSize = document.length()/1024
            fileNameLabel.text = document.name
            fileSizeLabel.fileSize(fileSize)
        }
    }
}
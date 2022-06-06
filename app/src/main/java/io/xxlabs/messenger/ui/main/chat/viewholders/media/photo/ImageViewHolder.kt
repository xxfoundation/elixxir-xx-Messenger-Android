package io.xxlabs.messenger.ui.main.chat.viewholders.media.photo

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder

/**
 * Superclass for sent and received image messages
 */
abstract class ImageViewHolder(parent: View): MessageViewHolder<PrivateMessage>(parent) {

    abstract val image: ImageView

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)

        image.setOnLongClickListener { listener.onLongClick(message); true }

        message.fileUri?.let { uri ->
            Glide.with(itemView.context)
                .load(uri)
                .into(image)

            image.setOnClickListener {
                listener.onImageClicked(uri)
            }
        }
    }
}
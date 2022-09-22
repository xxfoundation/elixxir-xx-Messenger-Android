package io.xxlabs.messenger.ui.main.chat.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.filetransfer.FileType
import io.xxlabs.messenger.media.MediaPlayerProvider
import io.xxlabs.messenger.ui.main.chat.PrivateMessagesViewModel
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.audio.ReceivedAudioViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.audio.SentAudioViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.file.ReceivedFileViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.file.SentFileViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.photo.ReceivedImageViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.photo.SentImageViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.video.ReceivedVideoViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.media.video.SentVideoViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.text.ReceivedMessageViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.text.SentMessageViewHolder

class PrivateMessagesAdapter(
    private val chatViewModel: PrivateMessagesViewModel,
    mediaPlayerProvider: MediaPlayerProvider
) : ChatMessagesAdapter<PrivateMessage, MessageViewHolder<PrivateMessage>>(PrivateMessageDiffCallback()) {

    private var listener = MessageItemListener(chatViewModel, mediaPlayerProvider)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getMessageId(position)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder<PrivateMessage> {
        return when (viewType) {
            R.layout.list_item_msg_sent -> SentMessageViewHolder.create(parent)
            R.layout.list_item_msg_photo_sent -> SentImageViewHolder.create(parent)
            R.layout.list_item_msg_photo_received -> ReceivedImageViewHolder.create(parent)
            R.layout.list_item_msg_audio_sent -> SentAudioViewHolder.create(parent)
            R.layout.list_item_msg_audio_received -> ReceivedAudioViewHolder.create(parent)
            R.layout.list_item_msg_video_sent -> SentVideoViewHolder.create(parent)
            R.layout.list_item_msg_video_received -> ReceivedVideoViewHolder.create(parent)
            R.layout.list_item_msg_file_sent -> SentFileViewHolder.create(parent)
            R.layout.list_item_msg_file_received -> ReceivedFileViewHolder.create(parent)
            else -> ReceivedMessageViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder<PrivateMessage>,
        position: Int
    ) {
        val header = showHeader(position)

        getItem(position)?.let {
            holder.onBind(it, listener, header, selectionMode, chatViewModel)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position) ?: return RecyclerView.NO_POSITION
        // TODO: "Other" file support
        return when (message.fileType) {
            FileType.AUDIO.toString() -> {
                when (message.status) {
                    MessageStatus.RECEIVED.value -> R.layout.list_item_msg_audio_received
                    else -> R.layout.list_item_msg_audio_sent
                }
            }
            FileType.VIDEO.toString() -> {
                when (message.status) {
                    MessageStatus.RECEIVED.value -> R.layout.list_item_msg_video_received
                    else -> R.layout.list_item_msg_video_sent
                }
            }
            FileType.IMAGE.toString() -> {
                when (message.status) {
                    MessageStatus.RECEIVED.value -> R.layout.list_item_msg_photo_received
                    else -> R.layout.list_item_msg_photo_sent
                }
            }
            FileType.DOCUMENT.toString(), FileType.OTHER.toString() -> {
                when (message.status) {
                    MessageStatus.RECEIVED.value -> R.layout.list_item_msg_file_received
                    else -> R.layout.list_item_msg_file_sent
                }
            }
            else -> {
                when (message.status) {
                    MessageStatus.RECEIVED.value -> R.layout.list_item_msg_received
                    else -> R.layout.list_item_msg_sent
                }
            }
        }
    }
}

/**
 * Handles diff calculations as the message list changes.
 */
class PrivateMessageDiffCallback : ChatMessageDiffCallback<PrivateMessage>() {
    override fun areItemsTheSame(oldItem: PrivateMessage, newItem: PrivateMessage): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: PrivateMessage, newItem: PrivateMessage): Boolean =
        oldItem == newItem
}

/**
 * Passes list item events to the ChatViewModel and MediaPlayerProvider.
 */
class MessageItemListener(
    private val chatViewModel: PrivateMessagesViewModel,
    private val mediaPlayerProvider: MediaPlayerProvider
) : MessageListener<PrivateMessage>, MediaPlayerProvider by mediaPlayerProvider {
    override fun onClick(message: PrivateMessage) =
        chatViewModel.onMessageClicked(message)

    override fun onLongClick(message: PrivateMessage) =
        chatViewModel.onMessageLongClicked(message)

    override fun onReplyClicked(reply: ReplyWrapper) =
        chatViewModel.onReplyPreviewClicked(reply.uniqueId)

    override fun lookupMessage(uniqueId: ByteArray): PrivateMessage? =
        chatViewModel.lookupMessage(uniqueId)

    override fun onShowMixClicked(message: PrivateMessage) {
        chatViewModel.onShowMixClicked(message)
    }

    override fun onImageClicked(imageUri: String) {
        chatViewModel.onImageClicked(imageUri)
    }
}
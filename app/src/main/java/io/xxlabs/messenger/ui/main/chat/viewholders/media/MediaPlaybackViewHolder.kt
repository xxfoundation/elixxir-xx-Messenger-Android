package io.xxlabs.messenger.ui.main.chat.viewholders.media

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.media.MediaPlayerProvider
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageViewHolder
import java.lang.ClassCastException

/**
 * Base presentation class for [PrivateMessageData] that have playable media
 * such as audio or video.
 */
abstract class MediaPlaybackViewHolder(parent: View): MessageViewHolder<PrivateMessage>(parent) {

    /* Media player initialization */

    private lateinit var mediaPlayerProvider: MediaPlayerProvider

    /* Exposed MediaPlayer properties */

    protected val duration: Int
        get() {
            return mediaPlayerProvider.duration
        }
    protected val currentPosition: Int
        get() {
            return mediaPlayerProvider.currentPosition
        }
    protected val isPlaying: Boolean
        get() {
            return mediaPlayerProvider.isPlaying
        }

    abstract val elapsedTimeCallback: () -> Unit
    abstract val onPlaybackStopped: () -> Unit

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)
        mediaPlayerProvider = (listener as? MediaPlayerProvider)
            ?: throw ClassCastException("Missing MediaPlayerProvider")
    }

    protected fun getDuration(fileUri: String): Int {
        val durationString = MediaMetadataRetriever().run {
            setDataSource(itemView.context, Uri.parse(fileUri))
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        }
        return durationString?.toInt() ?: 0
    }

    protected fun play(fileUri: String, volumeOn: Boolean = true) =
        mediaPlayerProvider.play(
            Uri.parse(fileUri),
            elapsedTimeCallback,
            onPlaybackStopped,
            startWithVolumeOn = volumeOn
        )

    protected fun pause() = mediaPlayerProvider.pause()

    protected fun seekTo(position: Int) = mediaPlayerProvider.seekTo(position)

    protected fun mute() = mediaPlayerProvider.mute()

    protected fun unMute() = mediaPlayerProvider.unMute()
}
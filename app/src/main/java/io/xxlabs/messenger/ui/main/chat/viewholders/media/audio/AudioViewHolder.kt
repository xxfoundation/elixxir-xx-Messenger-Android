package io.xxlabs.messenger.ui.main.chat.viewholders.media.audio

import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.viewholders.media.MediaPlaybackViewHolder
import io.xxlabs.messenger.ui.main.chat.viewholders.MessageListener
import io.xxlabs.messenger.ui.main.chat.elapsedTime

/**
 * Superclass for sent and received audio messages.
 */
abstract class AudioViewHolder(parent: View): MediaPlaybackViewHolder(parent) {

    abstract val audioPlayButton: ImageButton
    abstract val audioPauseButton: ImageButton
    abstract val audioSeekbar: SeekBar
    abstract val audioMuteButton: ImageButton
    abstract val audioUnmuteButton: ImageButton
    abstract val audioTimeLabel: TextView

    override val elapsedTimeCallback: () -> Unit = {
        audioSeekbar.max = duration
        audioSeekbar.progress = currentPosition
    }
    override val onPlaybackStopped: () -> Unit = {
        togglePlayback(false)
    }

    private var volumeOn: Boolean = false
    private lateinit var message: PrivateMessage

    override fun onBind(
        message: PrivateMessage,
        listener: MessageListener<PrivateMessage>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<PrivateMessage>
    ) {
        super.onBind(message, listener, header, selectionMode, chatViewModel)
        volumeOn = false
        this.message = message

        audioSeekbar.apply {
            setOnLongClickListener { listener.onLongClick(message); true }
            setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) seekTo(progress)

                    when (currentPosition) {
                        0 -> audioTimeLabel.elapsedTime(duration)
                        else -> audioTimeLabel.elapsedTime(currentPosition)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    togglePlayback(false)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    message.fileUri?.let { togglePlayback(true) }
                }
            })
        }

        audioPlayButton.apply {
            visibility = View.VISIBLE
            setOnClickListener { togglePlayback(true) }
            setOnLongClickListener { listener.onLongClick(message); true }
        }

        audioPauseButton.apply {
            visibility = View.GONE
            setOnClickListener { togglePlayback(false) }
            setOnLongClickListener { listener.onLongClick(message); true }
        }

        audioUnmuteButton.apply {
            visibility = View.VISIBLE
            setOnClickListener { toggleSound(true) }
            setOnLongClickListener { listener.onLongClick(message); true }
        }

        audioMuteButton.apply {
            visibility = View.GONE
            setOnClickListener { toggleSound(false) }
            setOnLongClickListener { listener.onLongClick(message); true }
        }

        message.fileUri?.let {
            audioTimeLabel.elapsedTime(getDuration(it))
        }
    }

    private fun togglePlayback(play: Boolean) {
        message.fileUri?.let {
            if (play) {
                play(it, volumeOn)
                audioPlayButton.visibility = View.INVISIBLE
                audioPauseButton.visibility = View.VISIBLE
            } else {
                if (isPlaying) pause()
                audioPlayButton.visibility = View.VISIBLE
                audioPauseButton.visibility = View.GONE
            }
        }
    }

    private fun toggleSound(on: Boolean) {
        volumeOn = on
        if (on) {
            unMute()
            audioMuteButton.visibility = View.VISIBLE
            audioUnmuteButton.visibility = View.INVISIBLE
        } else {
            mute()
            audioUnmuteButton.visibility = View.VISIBLE
            audioMuteButton.visibility = View.GONE
        }
    }
}
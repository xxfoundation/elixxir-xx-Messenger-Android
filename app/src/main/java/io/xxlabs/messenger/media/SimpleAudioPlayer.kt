package io.xxlabs.messenger.media

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.ui.main.chat.elapsedTime

interface SimpleMediaPlayerControls {
    val audioSeekBar: SeekBar
    val audioTimeLabel: TextView
    val audioPlayButton: View
    val audioPauseButton: View
}

class SimpleAudioPlayer(
    private val mediaPlayerProvider: MediaPlayerProvider,
    private val ui: SimpleMediaPlayerControls,
    private val volumeOn: Boolean = true
) : MediaPlayerProvider by mediaPlayerProvider,
    SimpleMediaPlayerControls by ui
{
    var audioUri: Uri = Uri.EMPTY
        set(value) {
            field = value
            if (field != Uri.EMPTY) {
                setAudioDuration()
            }
        }

    private val elapsedTimeCallback: () -> Unit = {
        ui.audioSeekBar.max = duration
        ui.audioSeekBar.progress = currentPosition
    }
    private val onPlaybackStopped: () -> Unit = {
        togglePlayback(false)
    }

    init {
        audioSeekBar.apply {
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
                    togglePlayback(true)
                }
            })
        }
    }

    private fun setAudioDuration() {
        MediaMetadataRetriever().run {
            setDataSource(appContext(), audioUri)
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        }
            ?.toInt()
            ?.apply { audioTimeLabel.elapsedTime(this) }
    }

    fun togglePlayback(play: Boolean) {
        if (play) {
            play(audioUri, volumeOn)
            ui.audioPlayButton.visibility = View.INVISIBLE
            ui.audioPauseButton.visibility = View.VISIBLE

        } else {
            if (isPlaying) pause()
            audioPlayButton.visibility = View.VISIBLE
            audioPauseButton.visibility = View.GONE
        }
    }

    private fun play(uri: Uri = audioUri, volumeOn: Boolean = true) =
        mediaPlayerProvider.play(
            uri,
            elapsedTimeCallback,
            onPlaybackStopped,
            startWithVolumeOn = volumeOn
        )
}
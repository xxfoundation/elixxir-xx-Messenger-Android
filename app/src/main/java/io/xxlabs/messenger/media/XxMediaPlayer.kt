package io.xxlabs.messenger.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.toast
import timber.log.Timber
import java.util.*

class XxMediaPlayer(lifecycleOwner: LifecycleOwner) : MediaPlayerProvider {

    /* Audio playback */

    // A single MediaPlayer instance used for playing audio/video for this lifecycleOwner
    private val mediaPlayer: MediaPlayer by lazy {
            MediaPlayer().apply {
            setOnErrorListener { _, what, _ ->
                appContext().toast("Audio player error. Code: $what")
                reset()
                false
            }
        }
    }

    override val duration: Int
        get() {
            return if (isPrepared) mediaPlayer.duration
            else 0
        }

    override val currentPosition: Int
        get() {
            return if (isPrepared) mediaPlayer.currentPosition
            else 0
        }
    override val isPlaying: Boolean
        get() {
            return if (isPrepared) mediaPlayer.isPlaying
            else false
        }

    private var isPrepared = false

    // Responsible for muting/un-muting audio messages
    private val audioManager: AudioManager by lazy {
        appContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // Called on each execution of the timer's run function (every second)
    private var elapsedTimeCallback: () -> Unit = { }
    // A single Timer instance to be shared among MediaPlaybackViewHolders.
    private var elapsedTime: Timer? = null
    // Called when playback is stopped outside of user interaction.
    private var onPlaybackStopped: () -> Unit = { }

    private val lifeCycleObserver = object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            resetMediaPlayer()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            cancelPlayback()
        }
    }

    init {
        lifecycleOwner.lifecycle.addObserver(lifeCycleObserver)
    }

    private fun resetMediaPlayer() {
        mediaPlayer.apply {
            // Reset the media player each time its instance is provided.
            resetTimer()
            isPrepared = false
            reset()
        }
    }

    private fun cancelPlayback() {
        mediaPlayer.release()
    }

    private fun initTimer() {
        elapsedTime = Timer().apply {
            scheduleAtFixedRate(object: TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        elapsedTimeCallback()
                    }
                }
            }, 0, 1000)
        }
    }

    private fun resetTimer() {
        onPlaybackStopped()
        elapsedTime?.cancel()
        elapsedTime = null
    }

    private var currentUri: Uri = Uri.EMPTY

    override fun play(
        uri: Uri,
        elapsedTimeCallback: () -> Unit,
        onPlaybackStopped: () -> Unit,
        startWithVolumeOn: Boolean
    ) {
        if (currentUri != uri) {
            resetMediaPlayer()
            currentUri = uri
            this.elapsedTimeCallback = elapsedTimeCallback
            this.onPlaybackStopped = onPlaybackStopped
        }

        mediaPlayer.apply {
            when {
                isPrepared -> startPlayback()
                isPlaying -> this@XxMediaPlayer.pause()
                else -> prepareAudio(uri, startWithVolumeOn)
            }
        }
    }

    private fun prepareAudio(uri: Uri, volumeOn: Boolean) {
        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener {
                isPrepared = true
                play(uri)
            }
            setOnCompletionListener {
                resetTimer()
            }
            setDataSource(
                appContext(),
                uri
            )

            if (volumeOn) unMute()
            else mute()

            prepare()
        }
    }

    private fun startPlayback() {
        initTimer()
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.apply {
            try {
                if (isPlaying) pause()
            } catch (e: IllegalStateException) {
                Timber.d("pause() : Illegal MediaPlayer state")
            }
        }
    }

    override fun seekTo(position: Int) {
        try {
            mediaPlayer.seekTo(position)
        } catch (e: IllegalStateException) {
            Timber.d("seekTo() : Illegal MediaPlayer state")
        }
    }

    override fun mute() {
        audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
    }

    override fun unMute() {
        audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
    }
}
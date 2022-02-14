package io.xxlabs.messenger.media

import android.net.Uri

interface MediaPlayerProvider {
    val duration: Int
    val currentPosition: Int
    val isPlaying: Boolean

    /**
     * Attempted to play the [uri] and pass an optional [elapsedTimeCallback]
     * that will be called once each second during playback and an
     * [onPlaybackStopped] function that is called when playback is stopped
     * outside of user interaction swaps media.
     */
    fun play(
        uri: Uri,
        elapsedTimeCallback: () -> Unit = {},
        onPlaybackStopped: () -> Unit = {},
        startWithVolumeOn: Boolean = true
    )
    fun pause()
    fun seekTo(position: Int)
    fun mute()
    fun unMute()
}
package io.xxlabs.messenger.media

interface MicrophoneCallback {
    /**
     * Called when record audio permission has been granted.
     */
    fun onMicrophonePermissionGranted()
}
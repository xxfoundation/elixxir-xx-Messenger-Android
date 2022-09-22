package io.xxlabs.messenger.media

interface CameraProvider {
    /**
     * Start the camera for a photo, or for video if [forVideo] is true.
     */
    fun startCamera(
        callback: MediaCallback,
        forVideo: Boolean = false
    )
}
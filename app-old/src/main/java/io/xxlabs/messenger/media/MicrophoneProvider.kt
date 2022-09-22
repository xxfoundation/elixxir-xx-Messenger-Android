package io.xxlabs.messenger.media

interface MicrophoneProvider {
    fun requestRecordAudioPermission(micCallback: MicrophoneCallback)
}
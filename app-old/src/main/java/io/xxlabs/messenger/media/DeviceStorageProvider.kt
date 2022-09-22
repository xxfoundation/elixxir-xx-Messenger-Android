package io.xxlabs.messenger.media

interface DeviceStorageProvider {
    /**
     * Display files from the device's storage that match the
     * one of the provided [mimeTypes] to allow selection of
     * one (or many if [multipleSelections] is true)
     */
    fun selectFiles(
        callback: MediaCallback,
        mimeTypes: List<String>,
        multipleSelections: Boolean = false
    )
}
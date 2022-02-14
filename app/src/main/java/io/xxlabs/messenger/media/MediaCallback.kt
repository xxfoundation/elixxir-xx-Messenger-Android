package io.xxlabs.messenger.media

import android.net.Uri

interface MediaCallback {
    /**
     * Returns a list of one or more files that were selected by the user.
     */
    fun onFilesSelected(uriList: List<Uri>)
}
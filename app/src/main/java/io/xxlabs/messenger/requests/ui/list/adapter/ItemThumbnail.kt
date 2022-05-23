package io.xxlabs.messenger.requests.ui.list.adapter

import android.graphics.Bitmap

interface ItemThumbnail {
    val itemPhoto: Bitmap?
    val itemIconRes: Int?
    val itemInitials: String?
}
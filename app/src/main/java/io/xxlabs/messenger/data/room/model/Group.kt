package io.xxlabs.messenger.data.room.model

import android.graphics.Bitmap
import io.xxlabs.messenger.R
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import java.io.Serializable

interface Group : Serializable {
    val id: Long
    val groupId: ByteArray
    val name: String
    val leader: ByteArray
    val serial: ByteArray
    val status: Int

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

val Group.thumbnail: ItemThumbnail get() {
    return object : ItemThumbnail {
        override val itemPhoto: Bitmap? = null
        override val itemIconRes: Int = R.drawable.ic_group_chat
        override val itemInitials: String? = null
    }
}
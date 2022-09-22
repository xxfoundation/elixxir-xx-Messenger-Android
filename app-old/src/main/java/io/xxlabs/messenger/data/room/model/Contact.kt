package io.xxlabs.messenger.data.room.model

import android.graphics.Bitmap
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.support.view.BitmapResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable

interface Contact : Serializable {
    val id: Long
    val userId: ByteArray
    val username: String
    val status: Int
    val nickname: String
    val photo: ByteArray?
    val email: String
    val phone: String
    val marshaled: ByteArray?
    val createdAt: Long
    val displayName: String
    val initials: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    fun hasFacts(): Boolean
}

fun Contact.formattedEmail(): String? =
    if (email.isNotBlank()) email.substring(1)
    else null

fun Contact.formattedPhone(flagEmoji: Boolean = false): String? =
    phone.ifBlank { null }

suspend fun Contact.resolveBitmap(): Bitmap? = withContext(Dispatchers.IO) {
    BitmapResolver.getBitmap(photo)
}

suspend fun Contact.generateThumbnail(): ItemThumbnail {
    val photo = resolveBitmap()
    return object : ItemThumbnail {
        override val itemPhoto: Bitmap? = photo
        override val itemIconRes: Int? = null
        override val itemInitials: String = initials
    }
}

fun Contact.dummyThumbnail(): ItemThumbnail =
    object : ItemThumbnail {
        override val itemPhoto: Bitmap? = null
        override val itemIconRes: Int? = null
        override val itemInitials: String? = initials
    }

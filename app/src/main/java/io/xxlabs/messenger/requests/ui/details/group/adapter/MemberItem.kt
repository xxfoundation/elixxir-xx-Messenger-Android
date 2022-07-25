package io.xxlabs.messenger.requests.ui.details.group.adapter

import android.graphics.Bitmap
import androidx.annotation.ColorRes
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.support.appContext

data class MemberItem(
    override val itemPhoto: Bitmap? = null,
    override val itemIconRes: Int? = null,
    override val itemInitials: String? = "XX",
    val name: String = "xxMessenger User",
    val isCreator: Boolean = false,
    private val isContact: Boolean = false,
) : ItemThumbnail {

    val description: String? = when {
        isCreator -> appContext().getString(R.string.group_member_creator)
        !isContact -> appContext().getString(R.string.group_member_not_connection)
        else -> null
    }

    @ColorRes
    val descriptionTextColor: Int = when {
        isCreator -> appContext().getColor(R.color.accent_safe)
        else -> appContext().getColor(R.color.neutral_secondary)
    }

    companion object {
        fun from(contact: Contact, group: Group, bitmap: Bitmap?) = MemberItem(
            itemPhoto = bitmap,
            itemInitials = contact.initials,
            name = contact.displayName,
            isCreator = group.leader.contentEquals(contact.userId),
            isContact = contact.status == RequestStatus.ACCEPTED.value
        )
    }
}
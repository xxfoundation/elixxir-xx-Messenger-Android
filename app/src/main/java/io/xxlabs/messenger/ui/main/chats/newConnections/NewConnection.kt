package io.xxlabs.messenger.ui.main.chats.newConnections

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.extensions.toBase64String

@Entity(tableName = "NewConnections")
open class NewConnection(
    @PrimaryKey
    @ColumnInfo(name = "userId") val userId: String = ""
)

data class NewConnectionData(
    private val listener: NewConnectionListener,
    override val contact: ContactData,
    private val photo: Bitmap? = null
) : NewConnection(contact.userId.toBase64String()), NewConnectionUI {
    override val itemPhoto: Bitmap? = photo
    override val itemIconRes: Int? = null
    override val itemInitials: String = contact.initials

    override fun onNewConnectionClicked(contact: ContactData) =
        listener.onNewConnectionClicked(contact)
}
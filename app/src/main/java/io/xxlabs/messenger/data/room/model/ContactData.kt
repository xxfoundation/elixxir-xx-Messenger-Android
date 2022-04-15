package io.xxlabs.messenger.data.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber
import java.util.*

@Entity(
    tableName = "Contacts",
    indices = [Index(value = ["userId"], unique = true)]
)
data class ContactData(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,
    @ColumnInfo(name = "userId", typeAffinity = ColumnInfo.BLOB)
    override var userId: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "username")
    override var username: String = "",
    @ColumnInfo(name = "status")
    override var status: Int = RequestStatus.SENT.value,
    @ColumnInfo(name = "name")
    override var nickname: String = "",
    @ColumnInfo(name = "photo", typeAffinity = ColumnInfo.BLOB)
    override var photo: ByteArray? = null,
    @ColumnInfo(name = "email")
    override var email: String = "",
    @ColumnInfo(name = "phone")
    override var phone: String = "",
    @ColumnInfo(name = "marshaled", typeAffinity = ColumnInfo.BLOB)
    override var marshaled: ByteArray? = null,
    @ColumnInfo(name = "createdAt", typeAffinity = ColumnInfo.INTEGER)
    override var createdAt: Long = Utils.getCurrentTimeStamp()
) : Contact {
    override val displayName: String
        get() = if (nickname.isNotEmpty()) nickname else username

    override val initials: String
        get() = displayName.let {
            try {
                val splitName = it.split(" ")
                if (splitName.size > 1) {
                    splitName[0][0].uppercase() + splitName[1][0].uppercase()
                } else {
                    it.substring(0, 2).uppercase()
                }
            } catch(e: Exception) {
                "??"
            }
        }

    override fun equals(other: Any?): Boolean {
        if (other !is ContactData) return false

        return when {
            !userId.contentEquals(other.userId)
                    || username != other.username
                    || status != other.status -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }

    override fun hasFacts(): Boolean {
        return email.isNotBlank() || phone.isNotBlank()
    }

    companion object : Comparator<Any> {
        fun from(
            contactWrapper: ContactWrapperBase,
            requestStatus: RequestStatus = RequestStatus.SENT
        ): ContactData = ContactData(
            marshaled = contactWrapper.marshal(),
            userId = contactWrapper.getId(),
            username = contactWrapper.getUsernameFact(),
            nickname = contactWrapper.getNameFact() ?: "",
            email = contactWrapper.getEmailFact() ?: "",
            phone = contactWrapper.getPhoneFact() ?: "",
            status = requestStatus.value
        )

        override fun compare(item: Any, anotherItem: Any): Int {
            val first = if (item is ContactData) {
                item.displayName
            } else {
                item as GroupData
                item.name
            }
            val second = if (anotherItem is ContactData) {
                anotherItem.displayName
            } else {
                anotherItem as GroupData
                anotherItem.name
            }

            Timber.v("Comparing $first x $second: ${first.compareTo(second)}")
            return first.compareTo(second, true)
        }
    }
}
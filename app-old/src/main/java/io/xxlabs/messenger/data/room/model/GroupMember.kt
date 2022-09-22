package io.xxlabs.messenger.data.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GroupMembers")
data class GroupMember(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "groupId", typeAffinity = ColumnInfo.BLOB)
    var groupId: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "member", typeAffinity = ColumnInfo.BLOB)
    var userId: ByteArray = byteArrayOf(),
    var username: String? = null
) {

    override fun equals(other: Any?): Boolean {
        if (other !is GroupMember) return false

        return when {
            id != other.id
                    || !groupId.contentEquals(other.groupId)
                    || !userId.contentEquals(other.userId) -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + groupId.contentHashCode()
        result = 31 * result + userId.contentHashCode()
        return result
    }
}
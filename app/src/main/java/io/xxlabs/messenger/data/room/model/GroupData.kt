package io.xxlabs.messenger.data.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.data.datatype.RequestStatus

@Entity(
    tableName = "Groups",
    indices = [Index(value = ["groupId"], unique = true)]
)
data class GroupData(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,
    @ColumnInfo(name = "groupId", typeAffinity = ColumnInfo.BLOB)
    override var groupId: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "name")
    override var name: String,
    @ColumnInfo(name = "leader", typeAffinity = ColumnInfo.BLOB)
    override var leader: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "serial", typeAffinity = ColumnInfo.BLOB)
    override var serial: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "status")
    override val status: Int = RequestStatus.SENT.value
) : Group {
    override fun equals(other: Any?): Boolean {
        if (other !is GroupData) return false

        return when {
            id != other.id
                    || groupId.contentEquals(other.groupId)
                    || leader.contentEquals(other.leader)
                    || status != other.status
                    || serial.contentEquals(other.serial) -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + groupId.contentHashCode()
        result = 31 * result + leader.contentHashCode()
        result = 31 * result + serial.contentHashCode()
        result = 31 * result + status.hashCode()
        return result
    }

    companion object {
        fun from(groupBindings: GroupBase, status: RequestStatus): GroupData =
            GroupData(
                groupId = groupBindings.getID(),
                name = groupBindings.getName().decodeToString(),
                leader = groupBindings.getMembership()[0].getID(),
                serial = groupBindings.serialize(),
                status = status.value
            )
    }
}
package io.xxlabs.messenger.data.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Messages")
data class PrivateMessageData(
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0,
    @ColumnInfo(name = "uniqueId", typeAffinity = ColumnInfo.BLOB)
    override var uniqueId: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "status")
    override var status: Int = 0,
    @ColumnInfo(name = "payload")
    override var payload: String = "",
    @ColumnInfo(name = "timestamp", typeAffinity = ColumnInfo.INTEGER)
    override var timestamp: Long = 0,
    @ColumnInfo(name = "unread")
    override var unread: Boolean = true,
    @ColumnInfo(name = "sender", typeAffinity = ColumnInfo.BLOB)
    override var sender: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "receiver", typeAffinity = ColumnInfo.BLOB)
    override var receiver: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "sentReport", typeAffinity = ColumnInfo.BLOB)
    override var sendReport: ByteArray? = null,
    @ColumnInfo(name = "fileType")
    override var fileType: String? = null,
    @ColumnInfo(name = "fileUri")
    override var fileUri: String? = null,
    @ColumnInfo(name = "transferProgress")
    override var transferProgress: Long = 100,
    @ColumnInfo(name = "roundUrl")
    override var roundUrl: String? = null
) : XxChatMessage(), PrivateMessage {

    override fun equals(other: Any?): Boolean {
        if (other !is PrivateMessageData) return false

        return when {
            id != other.id
                    || !uniqueId.contentEquals(other.uniqueId)
                    || !sender.contentEquals(other.sender)
                    || !receiver.contentEquals(other.receiver)
                    || !sendReport.contentEquals(other.sendReport)
                    || payload != other.payload
                    || timestamp != other.timestamp
                    || status != other.status
                    || unread != other.unread
                    || fileType != other.fileType
                    || fileUri != other.fileUri
                    || transferProgress != other.transferProgress -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uniqueId.hashCode()
        result = 31 * result + sender.contentHashCode()
        result = 31 * result + receiver.contentHashCode()
        result = 31 * result + payload.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + status
        result = 31 * result + unread.hashCode()
        result = 31 * result + (fileType?.hashCode() ?: id.hashCode())
        result = 31 * result + (fileUri?.hashCode() ?: id.hashCode())
        result = 31 * result + transferProgress.hashCode()
        return result
    }
}
package io.xxlabs.messenger.data.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.xxlabs.messenger.requests.model.Request

@Entity(tableName = "Requests")
data class RequestData(
    @PrimaryKey
    @ColumnInfo(name = "requestId", typeAffinity = ColumnInfo.BLOB)
    val requestId: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "createdAt", typeAffinity = ColumnInfo.INTEGER)
    val createdAt: Long = 0,
    @ColumnInfo(name = "unread")
    val unread: Boolean = true
) {

    companion object {
        fun from(request: Request): RequestData =
            RequestData(
                requestId = request.requestId,
                createdAt = System.currentTimeMillis(),
                unread = request.unread
            )
    }
}

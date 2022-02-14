package io.xxlabs.messenger.data.room.model.converters

import androidx.room.TypeConverter
import io.xxlabs.messenger.data.datatype.MessageStatus

class SentStatusConverter {
    @TypeConverter
    fun toSentStatus(value: Int): MessageStatus {
        return MessageStatus.values()[value]
    }

    @TypeConverter
    fun toInt(msgStatus: MessageStatus): Int {
        return msgStatus.value
    }
}
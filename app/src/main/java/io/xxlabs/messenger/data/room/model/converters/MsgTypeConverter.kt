package io.xxlabs.messenger.data.room.model.converters

import androidx.room.TypeConverter
import io.xxlabs.messenger.data.datatype.MsgType

class MsgTypeConverter {
    @TypeConverter
    fun toMsgType(value: Int): MsgType {
        return MsgType.values()[value]
    }

    @TypeConverter
    fun toInt(msgStatus: MsgType): Int {
        return msgStatus.value
    }
}
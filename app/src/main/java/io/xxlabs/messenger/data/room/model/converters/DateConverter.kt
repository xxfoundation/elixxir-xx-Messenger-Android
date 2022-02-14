package io.xxlabs.messenger.data.room.model.converters

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
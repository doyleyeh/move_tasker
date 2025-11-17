package com.example.autosorter.data.local

import androidx.room.TypeConverter
import com.example.autosorter.data.entity.FileTypeFilter
import com.example.autosorter.data.entity.MoveStatus

class Converters {
    @TypeConverter
    fun fromFileTypeFilter(filter: FileTypeFilter): String = filter.name

    @TypeConverter
    fun toFileTypeFilter(value: String): FileTypeFilter = FileTypeFilter.valueOf(value)

    @TypeConverter
    fun fromMoveStatus(status: MoveStatus): String = status.name

    @TypeConverter
    fun toMoveStatus(value: String): MoveStatus = MoveStatus.valueOf(value)
}

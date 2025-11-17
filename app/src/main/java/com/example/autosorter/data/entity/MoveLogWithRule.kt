package com.example.autosorter.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class MoveLogWithRule(
    @Embedded val log: MoveLogEntity,
    @ColumnInfo(name = "ruleName") val ruleName: String?
)

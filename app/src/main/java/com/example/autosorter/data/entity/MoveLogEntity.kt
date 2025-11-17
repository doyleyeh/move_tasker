package com.example.autosorter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "move_logs")
data class MoveLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val sourcePath: String,
    val destinationPath: String,
    val ruleId: Long,
    val status: MoveStatus,
    val errorMessage: String? = null
)

package com.example.autosorter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sourcePath: String,
    val destinationPath: String,
    val fileTypeFilter: FileTypeFilter = FileTypeFilter.ALL,
    val extensionsFilter: String? = null,
    val enabled: Boolean = true
)

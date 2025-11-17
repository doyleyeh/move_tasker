package com.example.movetasker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movetasker.data.entity.MoveLogEntity
import com.example.movetasker.data.entity.MoveLogWithRule
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MoveLogEntity)

    @Query("SELECT * FROM move_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<MoveLogEntity>>

    @Query(
        "SELECT move_logs.*, rules.name AS ruleName FROM move_logs " +
            "LEFT JOIN rules ON move_logs.ruleId = rules.id " +
            "ORDER BY timestamp DESC LIMIT :limit"
    )
    fun getRecentLogsWithRule(limit: Int): Flow<List<MoveLogWithRule>>

    @Query("SELECT EXISTS(SELECT 1 FROM move_logs WHERE sourcePath = :sourcePath AND status = 'SUCCESS')")
    suspend fun hasLogForSource(sourcePath: String): Boolean
}

package com.example.movetasker.data.repository

import com.example.movetasker.data.dao.MoveLogDao
import com.example.movetasker.data.dao.RuleDao
import com.example.movetasker.data.entity.MoveLogEntity
import com.example.movetasker.data.entity.MoveLogWithRule
import com.example.movetasker.data.entity.MoveStatus
import com.example.movetasker.data.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

class MoveTaskerRepository(
    private val ruleDao: RuleDao,
    private val moveLogDao: MoveLogDao
) {
    fun getRules(): Flow<List<RuleEntity>> = ruleDao.getAllRules()

    fun getEnabledRules(): Flow<List<RuleEntity>> = ruleDao.getEnabledRules()

    suspend fun getEnabledRulesOnce(): List<RuleEntity> = ruleDao.getEnabledRulesOnce()

    suspend fun getRuleById(id: Long): RuleEntity? = ruleDao.getRuleById(id)

    fun observeRule(id: Long): Flow<RuleEntity?> = ruleDao.observeRuleById(id)

    suspend fun insertRule(rule: RuleEntity) = ruleDao.insertRule(rule)

    suspend fun updateRule(rule: RuleEntity) = ruleDao.updateRule(rule)

    suspend fun deleteRule(rule: RuleEntity) = ruleDao.deleteRule(rule)

    suspend fun insertLog(log: MoveLogEntity) = moveLogDao.insertLog(log)

    fun getRecentLogs(limit: Int): Flow<List<MoveLogEntity>> = moveLogDao.getRecentLogs(limit)

    fun getRecentLogsWithRule(limit: Int): Flow<List<MoveLogWithRule>> = moveLogDao.getRecentLogsWithRule(limit)

    suspend fun hasLogForSource(sourcePath: String): Boolean = moveLogDao.hasLogForSource(sourcePath)

    suspend fun saveMoveResult(
        rule: RuleEntity,
        sourceFilePath: String,
        destinationFilePath: String,
        status: MoveStatus,
        error: String? = null
    ) {
        val log = MoveLogEntity(
            timestamp = System.currentTimeMillis(),
            sourcePath = sourceFilePath,
            destinationPath = destinationFilePath,
            ruleId = rule.id,
            status = status,
            errorMessage = error
        )
        insertLog(log)
    }
}

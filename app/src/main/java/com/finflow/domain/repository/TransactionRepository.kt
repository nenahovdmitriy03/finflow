package com.finflow.domain.repository

import com.finflow.domain.model.Transaction
import com.finflow.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Transaction>>
    fun observeBalance(): Flow<Double>
    fun observeTotalByType(type: TransactionType, fromEpochMillis: Long, toEpochMillis: Long): Flow<Double>
    suspend fun add(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun deleteById(id: Long)
}

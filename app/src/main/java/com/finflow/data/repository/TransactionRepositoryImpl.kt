package com.finflow.data.repository

import com.finflow.data.local.dao.TransactionDao
import com.finflow.data.local.mapper.toDomain
import com.finflow.data.local.mapper.toEntity
import com.finflow.domain.model.Transaction
import com.finflow.domain.model.TransactionType
import com.finflow.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {
    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }
    override fun observeInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<Transaction>> =
        dao.observeInRange(fromEpochMillis, toEpochMillis).map { list -> list.map { it.toDomain() } }
    override fun observeBalance(): Flow<Double> = dao.observeBalance()
    override fun observeTotalByType(type: TransactionType, fromEpochMillis: Long, toEpochMillis: Long): Flow<Double> =
        dao.observeTotalByType(type.name, fromEpochMillis, toEpochMillis)
    override suspend fun add(transaction: Transaction): Long = dao.insert(transaction.toEntity())
    override suspend fun update(transaction: Transaction) = dao.update(transaction.toEntity())
    override suspend fun delete(transaction: Transaction) = dao.delete(transaction.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}

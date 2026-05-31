package com.finflow.data.repository

import com.finflow.data.local.dao.BudgetDao
import com.finflow.data.local.mapper.toDomain
import com.finflow.data.local.mapper.toEntity
import com.finflow.domain.model.Budget
import com.finflow.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
) : BudgetRepository {
    override fun observeAll(): Flow<List<Budget>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getByCategory(categoryId: Long): Budget? =
        dao.getByCategory(categoryId)?.toDomain()
    override suspend fun getById(id: Long): Budget? = dao.getById(id)?.toDomain()
    override suspend fun add(budget: Budget): Long = dao.insert(budget.toEntity())
    override suspend fun update(budget: Budget) = dao.update(budget.toEntity())
    override suspend fun delete(budget: Budget) = dao.delete(budget.toEntity())
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}

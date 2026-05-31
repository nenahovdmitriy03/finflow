package com.finflow.data.repository

import com.finflow.data.local.dao.GoalDao
import com.finflow.data.local.mapper.toDomain
import com.finflow.data.local.mapper.toEntity
import com.finflow.domain.model.Goal
import com.finflow.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao,
) : GoalRepository {
    override fun observeActive(): Flow<List<Goal>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }
    override fun observeAll(): Flow<List<Goal>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Long): Goal? = dao.getById(id)?.toDomain()
    override suspend fun add(goal: Goal): Long = dao.insert(goal.toEntity())
    override suspend fun update(goal: Goal) = dao.update(goal.toEntity())
    override suspend fun delete(goal: Goal) = dao.delete(goal.toEntity())
}

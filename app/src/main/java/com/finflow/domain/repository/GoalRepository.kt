package com.finflow.domain.repository

import com.finflow.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeActive(): Flow<List<Goal>>
    fun observeAll(): Flow<List<Goal>>
    suspend fun getById(id: Long): Goal?
    suspend fun add(goal: Goal): Long
    suspend fun update(goal: Goal)
    suspend fun delete(goal: Goal)
}

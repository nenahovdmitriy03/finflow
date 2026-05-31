package com.finflow.domain.repository

import com.finflow.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>
    suspend fun getByCategory(categoryId: Long): Budget?
    suspend fun getById(id: Long): Budget?
    suspend fun add(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
    suspend fun deleteById(id: Long)
}

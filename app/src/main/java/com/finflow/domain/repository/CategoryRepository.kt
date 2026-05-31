package com.finflow.domain.repository

import com.finflow.domain.model.Category
import com.finflow.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeByType(type: TransactionType): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun add(category: Category): Long
    suspend fun delete(category: Category)
}

package com.finflow.data.repository

import com.finflow.data.local.dao.CategoryDao
import com.finflow.data.local.mapper.toDomain
import com.finflow.data.local.mapper.toEntity
import com.finflow.domain.model.Category
import com.finflow.domain.model.TransactionType
import com.finflow.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }
    override fun observeByType(type: TransactionType): Flow<List<Category>> =
        dao.observeByType(type.name).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Long): Category? = dao.getById(id)?.toDomain()
    override suspend fun add(category: Category): Long = dao.insert(category.toEntity())
    override suspend fun delete(category: Category) = dao.delete(category.toEntity())
}

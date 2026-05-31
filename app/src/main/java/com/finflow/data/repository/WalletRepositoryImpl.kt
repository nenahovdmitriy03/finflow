package com.finflow.data.repository

import com.finflow.data.local.dao.WalletDao
import com.finflow.data.local.mapper.toDomain
import com.finflow.data.local.mapper.toEntity
import com.finflow.domain.model.Wallet
import com.finflow.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val dao: WalletDao,
) : WalletRepository {
    override fun observeAll(): Flow<List<Wallet>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Long): Wallet? = dao.getById(id)?.toDomain()
    override suspend fun add(wallet: Wallet): Long = dao.insert(wallet.toEntity())
    override suspend fun update(wallet: Wallet) = dao.update(wallet.toEntity())
    override suspend fun delete(wallet: Wallet) = dao.delete(wallet.toEntity())
}

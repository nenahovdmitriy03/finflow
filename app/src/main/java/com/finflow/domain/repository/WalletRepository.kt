package com.finflow.domain.repository

import com.finflow.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeAll(): Flow<List<Wallet>>
    suspend fun getById(id: Long): Wallet?
    suspend fun add(wallet: Wallet): Long
    suspend fun update(wallet: Wallet)
    suspend fun delete(wallet: Wallet)
}

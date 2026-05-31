package com.finflow.di

import com.finflow.data.repository.CategoryRepositoryImpl
import com.finflow.data.repository.GoalRepositoryImpl
import com.finflow.data.repository.TransactionRepositoryImpl
import com.finflow.data.repository.WalletRepositoryImpl
import com.finflow.domain.repository.CategoryRepository
import com.finflow.domain.repository.GoalRepository
import com.finflow.domain.repository.TransactionRepository
import com.finflow.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository
    @Binds @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository
}

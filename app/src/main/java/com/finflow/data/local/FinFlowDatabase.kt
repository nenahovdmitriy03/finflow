package com.finflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finflow.data.local.dao.BudgetDao
import com.finflow.data.local.dao.CategoryDao
import com.finflow.data.local.dao.GoalDao
import com.finflow.data.local.dao.TransactionDao
import com.finflow.data.local.dao.WalletDao
import com.finflow.data.local.entity.BudgetEntity
import com.finflow.data.local.entity.CategoryEntity
import com.finflow.data.local.entity.GoalEntity
import com.finflow.data.local.entity.TransactionEntity
import com.finflow.data.local.entity.WalletEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        WalletEntity::class,
        GoalEntity::class,
        BudgetEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class FinFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun walletDao(): WalletDao
    abstract fun goalDao(): GoalDao
    abstract fun budgetDao(): BudgetDao
}

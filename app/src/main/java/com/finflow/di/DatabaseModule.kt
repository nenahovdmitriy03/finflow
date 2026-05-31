package com.finflow.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.finflow.data.local.FinFlowDatabase
import com.finflow.data.local.dao.CategoryDao
import com.finflow.data.local.dao.GoalDao
import com.finflow.data.local.dao.TransactionDao
import com.finflow.data.local.dao.WalletDao
import com.finflow.data.local.seed.DefaultData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinFlowDatabase {
        lateinit var db: FinFlowDatabase
        db = Room.databaseBuilder(context, FinFlowDatabase::class.java, "finflow.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(connection: SupportSQLiteDatabase) {
                    super.onCreate(connection)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        with(db) {
                            categoryDao().insertAll(DefaultData.expenseCategories)
                            categoryDao().insertAll(DefaultData.incomeCategories)
                            walletDao().insertAll(DefaultData.defaultWallets)
                        }
                    }
                }
            })
            .build()
        return db
    }

    @Provides fun provideTransactionDao(db: FinFlowDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideCategoryDao(db: FinFlowDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideWalletDao(db: FinFlowDatabase): WalletDao = db.walletDao()
    @Provides fun provideGoalDao(db: FinFlowDatabase): GoalDao = db.goalDao()
}

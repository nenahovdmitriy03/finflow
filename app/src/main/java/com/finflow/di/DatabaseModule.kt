package com.finflow.di

import android.content.Context
import androidx.room.Room
import com.finflow.data.local.FinFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinFlowDatabase =
        Room.databaseBuilder(context, FinFlowDatabase::class.java, "finflow.db").build()
}

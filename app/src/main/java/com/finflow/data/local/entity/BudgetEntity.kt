package com.finflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Double,
    val period: String, // MONTHLY | WEEKLY
    val startEpochDay: Long, // период считаем от этой даты
    val notify80: Boolean = true,
    val notify100: Boolean = true,
)

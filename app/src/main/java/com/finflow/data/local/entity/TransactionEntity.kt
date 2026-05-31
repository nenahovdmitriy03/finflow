package com.finflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val walletId: Long,
    val dateEpochMillis: Long,
    val note: String? = null,
    val receiptPhotoPath: String? = null,
)

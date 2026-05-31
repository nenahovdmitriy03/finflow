package com.finflow.domain.model

import java.time.Instant

enum class TransactionType { EXPENSE, INCOME }

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val walletId: Long,
    val date: Instant,
    val note: String? = null,
    val receiptPhotoPath: String? = null,
)

package com.finflow.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val type: TransactionType,
    val isCustom: Boolean = false,
)

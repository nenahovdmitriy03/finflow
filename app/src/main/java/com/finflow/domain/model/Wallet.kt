package com.finflow.domain.model

enum class WalletKind { CASH, CARD, SAVINGS, CRYPTO, OTHER }

data class Wallet(
    val id: Long = 0,
    val name: String,
    val kind: WalletKind,
    val initialBalance: Double,
    val currency: String = "RUB",
)

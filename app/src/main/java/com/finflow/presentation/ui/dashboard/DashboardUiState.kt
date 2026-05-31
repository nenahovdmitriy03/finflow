package com.finflow.presentation.ui.dashboard

import com.finflow.domain.model.Category
import com.finflow.domain.model.Transaction

data class DashboardUiState(
    val isLoading: Boolean = true,
    val balance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val recentTransactions: List<TransactionUi> = emptyList(),
)

data class TransactionUi(
    val transaction: Transaction,
    val category: Category?,
)

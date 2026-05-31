package com.finflow.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.domain.model.TransactionType
import com.finflow.domain.repository.CategoryRepository
import com.finflow.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()

    val uiState: StateFlow<DashboardUiState> = combine(
        transactionRepository.observeBalance(),
        transactionRepository.observeTotalByType(TransactionType.INCOME, monthStart(), monthEnd()),
        transactionRepository.observeTotalByType(TransactionType.EXPENSE, monthStart(), monthEnd()),
        transactionRepository.observeRecent(10),
        categoryRepository.observeAll(),
    ) { balance, income, expense, transactions, categories ->
        val byId = categories.associateBy { it.id }
        DashboardUiState(
            isLoading = false,
            balance = balance,
            monthIncome = income,
            monthExpense = expense,
            recentTransactions = transactions.map { tx ->
                TransactionUi(transaction = tx, category = byId[tx.categoryId])
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DashboardUiState(),
    )

    private fun monthStart(): Long =
        YearMonth.now(zone).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun monthEnd(): Long =
        YearMonth.now(zone).atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
}

package com.finflow.presentation.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.domain.model.Category
import com.finflow.domain.model.TransactionType
import com.finflow.domain.repository.CategoryRepository
import com.finflow.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CategorySlice(
    val category: Category,
    val amount: Double,
    val share: Float,
)

data class DailySpend(val day: Int, val amount: Double)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenseByCategory: List<CategorySlice> = emptyList(),
    val dailyExpense: List<DailySpend> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val _month = MutableStateFlow(YearMonth.now(zone))

    val uiState: StateFlow<AnalyticsUiState> = _month.flatMapLatest { month ->
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

        combine(
            transactionRepository.observeInRange(start, end),
            categoryRepository.observeAll(),
        ) { transactions, categories ->
            val byId = categories.associateBy { it.id }
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            val byCat = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryId }
                .mapNotNull { (catId, txs) ->
                    val cat = byId[catId] ?: return@mapNotNull null
                    val total = txs.sumOf { it.amount }
                    CategorySlice(
                        category = cat,
                        amount = total,
                        share = if (expense > 0) (total / expense).toFloat() else 0f,
                    )
                }
                .sortedByDescending { it.amount }

            val daily = (1..month.lengthOfMonth()).map { day ->
                DailySpend(day = day, amount = 0.0)
            }.toMutableList()
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .forEach { tx ->
                    val d = tx.date.atZone(zone).toLocalDate()
                    if (d.year == month.year && d.monthValue == month.monthValue) {
                        val idx = d.dayOfMonth - 1
                        daily[idx] = daily[idx].copy(amount = daily[idx].amount + tx.amount)
                    }
                }

            AnalyticsUiState(
                isLoading = false,
                month = month,
                totalIncome = income,
                totalExpense = expense,
                expenseByCategory = byCat,
                dailyExpense = daily,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AnalyticsUiState(),
    )

    fun previousMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() {
        val next = _month.value.plusMonths(1)
        if (!next.isAfter(YearMonth.now(zone))) _month.value = next
    }
}

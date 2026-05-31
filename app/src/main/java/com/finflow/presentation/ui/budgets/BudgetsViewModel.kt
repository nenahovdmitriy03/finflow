package com.finflow.presentation.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.domain.model.Budget
import com.finflow.domain.model.BudgetPeriod
import com.finflow.domain.model.Category
import com.finflow.domain.model.TransactionType
import com.finflow.domain.repository.BudgetRepository
import com.finflow.domain.repository.CategoryRepository
import com.finflow.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class BudgetCard(
    val budget: Budget,
    val category: Category?,
    val spent: Double,
    val progress: Float,
    val remaining: Double,
    val isOverLimit: Boolean,
    val isWarning: Boolean,
)

data class BudgetsUiState(
    val isLoading: Boolean = true,
    val cards: List<BudgetCard> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
)

data class CreateBudgetForm(
    val categoryId: Long? = null,
    val limitText: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
) {
    val limitValue: Double? get() = limitText.replace(",", ".").toDoubleOrNull()
    val isValid: Boolean get() = categoryId != null && (limitValue ?: 0.0) > 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(CreateBudgetForm())
    private val _sheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.observeAll(),
        categoryRepository.observeAll(),
    ) { budgets, categories ->
        budgets to categories
    }.flatMapLatest { (budgets, categories) ->
        val today = LocalDate.now()
        val expenseCats = categories.filter { it.type == TransactionType.EXPENSE }
        val usedCategoryIds = budgets.map { it.categoryId }.toSet()
        val available = expenseCats.filter { it.id !in usedCategoryIds }

        if (budgets.isEmpty()) {
            flowOf(
                BudgetsUiState(
                    isLoading = false,
                    cards = emptyList(),
                    availableCategories = available,
                )
            )
        } else {
            val flows = budgets.map { budget ->
                val (from, to) = periodRange(budget, today)
                combine(
                    flowOf(budget),
                    transactionRepository.observeInRange(from, to),
                ) { b, txs ->
                    val spent = txs
                        .filter { it.type == TransactionType.EXPENSE && it.categoryId == b.categoryId }
                        .sumOf { it.amount }
                    val cat = categories.firstOrNull { it.id == b.categoryId }
                    val progress = if (b.limitAmount > 0) (spent / b.limitAmount).toFloat() else 0f
                    val remaining = b.limitAmount - spent
                    BudgetCard(
                        budget = b,
                        category = cat,
                        spent = spent,
                        progress = progress,
                        remaining = remaining,
                        isOverLimit = spent >= b.limitAmount,
                        isWarning = progress >= 0.8f && progress < 1f,
                    )
                }
            }
            combine(flows) { arr ->
                BudgetsUiState(
                    isLoading = false,
                    cards = arr.toList(),
                    availableCategories = available,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = BudgetsUiState(),
    )

    val sheetVisible: StateFlow<Boolean> = _sheetVisible.asStateFlow()
    val form: StateFlow<CreateBudgetForm> = _form.asStateFlow()

    fun openCreate() { _form.value = CreateBudgetForm(); _sheetVisible.value = true }
    fun closeCreate() { _sheetVisible.value = false }

    fun setCategory(id: Long) { _form.value = _form.value.copy(categoryId = id) }
    fun setLimit(v: String) {
        val cleaned = v.filter { it.isDigit() || it == '.' || it == ',' }
        _form.value = _form.value.copy(limitText = cleaned)
    }
    fun setPeriod(p: BudgetPeriod) { _form.value = _form.value.copy(period = p) }

    fun saveBudget() {
        val f = _form.value
        if (!f.isValid) return
        viewModelScope.launch {
            budgetRepository.add(
                Budget(
                    categoryId = f.categoryId!!,
                    limitAmount = f.limitValue ?: 0.0,
                    period = f.period,
                    start = LocalDate.now().withDayOfMonth(1),
                )
            )
            _sheetVisible.value = false
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { budgetRepository.deleteById(id) }
    }

    private fun periodRange(budget: Budget, today: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        return when (budget.period) {
            BudgetPeriod.MONTHLY -> {
                val ym = YearMonth.from(today)
                val from = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val to = ym.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                from to to
            }
            BudgetPeriod.WEEKLY -> {
                val weekStart = today.with(java.time.DayOfWeek.MONDAY)
                val from = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
                val to = weekStart.plusDays(6).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                from to to
            }
        }
    }
}

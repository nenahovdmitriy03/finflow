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
    val allExpenseCategories: List<Category> = emptyList(),
)

data class BudgetForm(
    val editingId: Long? = null,
    val categoryId: Long? = null,
    val limitText: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
) {
    val limitValue: Double? get() = limitText.replace(",", ".").toDoubleOrNull()
    val isValid: Boolean get() = categoryId != null && (limitValue ?: 0.0) > 0.0
    val isEditing: Boolean get() = editingId != null
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(BudgetForm())
    private val _sheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.observeAll(),
        categoryRepository.observeAll(),
    ) { budgets, categories -> budgets to categories }
        .flatMapLatest { (budgets, categories) ->
            val today = LocalDate.now()
            val expenseCats = categories.filter { it.type == TransactionType.EXPENSE }
            val used = budgets.map { it.categoryId }.toSet()
            val available = expenseCats.filter { it.id !in used }

            if (budgets.isEmpty()) {
                flowOf(
                    BudgetsUiState(
                        isLoading = false,
                        cards = emptyList(),
                        availableCategories = available,
                        allExpenseCategories = expenseCats,
                    )
                )
            } else {
                val flows = budgets.map { b ->
                    val (from, to) = periodRange(b, today)
                    combine(flowOf(b), transactionRepository.observeInRange(from, to)) { bb, txs ->
                        val spent = txs.filter { it.type == TransactionType.EXPENSE && it.categoryId == bb.categoryId }
                            .sumOf { it.amount }
                        val cat = categories.firstOrNull { it.id == bb.categoryId }
                        val progress = if (bb.limitAmount > 0) (spent / bb.limitAmount).toFloat() else 0f
                        val remaining = bb.limitAmount - spent
                        BudgetCard(
                            budget = bb, category = cat,
                            spent = spent, progress = progress, remaining = remaining,
                            isOverLimit = spent >= bb.limitAmount,
                            isWarning = progress >= 0.8f && progress < 1f,
                        )
                    }
                }
                combine(flows) { arr ->
                    BudgetsUiState(
                        isLoading = false,
                        cards = arr.toList(),
                        availableCategories = available,
                        allExpenseCategories = expenseCats,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), BudgetsUiState())

    val sheetVisible: StateFlow<Boolean> = _sheetVisible.asStateFlow()
    val form: StateFlow<BudgetForm> = _form.asStateFlow()

    fun openCreate() { _form.value = BudgetForm(); _sheetVisible.value = true }
    fun openEdit(card: BudgetCard) {
        _form.value = BudgetForm(
            editingId = card.budget.id,
            categoryId = card.budget.categoryId,
            limitText = card.budget.limitAmount.toString().removeSuffix(".0"),
            period = card.budget.period,
        )
        _sheetVisible.value = true
    }
    fun closeSheet() { _sheetVisible.value = false }
    fun setCategory(id: Long) { _form.value = _form.value.copy(categoryId = id) }
    fun setLimit(v: String) {
        val c = v.filter { it.isDigit() || it == '.' || it == ',' }
        _form.value = _form.value.copy(limitText = c)
    }
    fun setPeriod(p: BudgetPeriod) { _form.value = _form.value.copy(period = p) }

    fun saveBudget() {
        val f = _form.value
        if (!f.isValid) return
        viewModelScope.launch {
            if (f.isEditing) {
                val existing = budgetRepository.getById(f.editingId!!) ?: return@launch
                budgetRepository.update(
                    existing.copy(
                        categoryId = f.categoryId!!,
                        limitAmount = f.limitValue ?: 0.0,
                        period = f.period,
                    )
                )
            } else {
                budgetRepository.add(
                    Budget(
                        categoryId = f.categoryId!!,
                        limitAmount = f.limitValue ?: 0.0,
                        period = f.period,
                        start = LocalDate.now().withDayOfMonth(1),
                    )
                )
            }
            _sheetVisible.value = false
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { budgetRepository.deleteById(id) }
    }

    private fun periodRange(b: Budget, today: LocalDate): Pair<Long, Long> {
        val z = ZoneId.systemDefault()
        return when (b.period) {
            BudgetPeriod.MONTHLY -> {
                val ym = YearMonth.from(today)
                ym.atDay(1).atStartOfDay(z).toInstant().toEpochMilli() to
                    ym.atEndOfMonth().atTime(23, 59, 59).atZone(z).toInstant().toEpochMilli()
            }
            BudgetPeriod.WEEKLY -> {
                val ws = today.with(java.time.DayOfWeek.MONDAY)
                ws.atStartOfDay(z).toInstant().toEpochMilli() to
                    ws.plusDays(6).atTime(23, 59, 59).atZone(z).toInstant().toEpochMilli()
            }
        }
    }
}

package com.finflow.presentation.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.domain.model.Goal
import com.finflow.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil

data class GoalCard(
    val goal: Goal,
    val progress: Float,
    val remaining: Double,
    val monthsLeft: Int?,
    val monthlySuggestion: Double?,
    val isOverdue: Boolean,
)

data class GoalsUiState(
    val isLoading: Boolean = true,
    val cards: List<GoalCard> = emptyList(),
)

data class GoalForm(
    val editingId: Long? = null,
    val title: String = "",
    val targetText: String = "",
    val icon: String = "T",
    val colorHex: String = "#6C63FF",
    val deadline: LocalDate? = null,
) {
    val targetValue: Double? get() = targetText.replace(",", ".").toDoubleOrNull()
    val isValid: Boolean get() = title.isNotBlank() && (targetValue ?: 0.0) > 0.0
    val isEditing: Boolean get() = editingId != null
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(GoalForm())
    private val _sheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<GoalsUiState> = goalRepository.observeActive()
        .map { goals -> GoalsUiState(isLoading = false, cards = goals.map { it.toCard() }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), GoalsUiState())

    val sheetVisible: StateFlow<Boolean> = _sheetVisible.asStateFlow()
    val form: StateFlow<GoalForm> = _form.asStateFlow()

    fun openCreate() { _form.value = GoalForm(); _sheetVisible.value = true }
    fun openEdit(card: GoalCard) {
        _form.value = GoalForm(
            editingId = card.goal.id,
            title = card.goal.title,
            targetText = card.goal.targetAmount.toString().removeSuffix(".0"),
            icon = card.goal.icon,
            colorHex = card.goal.colorHex,
            deadline = card.goal.deadline,
        )
        _sheetVisible.value = true
    }
    fun closeSheet() { _sheetVisible.value = false }
    fun setTitle(v: String) { _form.value = _form.value.copy(title = v) }
    fun setTarget(v: String) {
        val c = v.filter { it.isDigit() || it == '.' || it == ',' }
        _form.value = _form.value.copy(targetText = c)
    }
    fun setIcon(v: String) { _form.value = _form.value.copy(icon = v) }
    fun setColor(v: String) { _form.value = _form.value.copy(colorHex = v) }
    fun setDeadline(v: LocalDate?) { _form.value = _form.value.copy(deadline = v) }

    fun saveGoal() {
        val f = _form.value
        if (!f.isValid) return
        viewModelScope.launch {
            if (f.isEditing) {
                val existing = goalRepository.getById(f.editingId!!) ?: return@launch
                goalRepository.update(
                    existing.copy(
                        title = f.title.trim(),
                        targetAmount = f.targetValue ?: 0.0,
                        deadline = f.deadline,
                        icon = f.icon,
                        colorHex = f.colorHex,
                    )
                )
            } else {
                goalRepository.add(
                    Goal(
                        title = f.title.trim(),
                        targetAmount = f.targetValue ?: 0.0,
                        currentAmount = 0.0,
                        deadline = f.deadline,
                        icon = f.icon,
                        colorHex = f.colorHex,
                    )
                )
            }
            _sheetVisible.value = false
        }
    }

    fun addContribution(goalId: Long, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val g = goalRepository.getById(goalId) ?: return@launch
            goalRepository.update(g.copy(currentAmount = (g.currentAmount + amount).coerceAtMost(g.targetAmount)))
        }
    }

    fun archiveGoal(goalId: Long) {
        viewModelScope.launch {
            val g = goalRepository.getById(goalId) ?: return@launch
            goalRepository.update(g.copy(isArchived = true))
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            val g = goalRepository.getById(goalId) ?: return@launch
            goalRepository.delete(g)
        }
    }

    private fun Goal.toCard(): GoalCard {
        val today = LocalDate.now()
        val progress = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
        val remaining = (targetAmount - currentAmount).coerceAtLeast(0.0)
        val months = deadline?.let { ChronoUnit.MONTHS.between(today.withDayOfMonth(1), it.withDayOfMonth(1)).toInt() }
        val isOverdue = deadline?.isBefore(today) == true && remaining > 0
        val monthly = if (months != null && months > 0 && remaining > 0) ceil(remaining / months) else null
        return GoalCard(this, progress, remaining, months, monthly, isOverdue)
    }
}

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
    val showCreateSheet: Boolean = false,
    val createForm: CreateGoalForm = CreateGoalForm(),
)

data class CreateGoalForm(
    val title: String = "",
    val targetText: String = "",
    val icon: String = "🎯",
    val colorHex: String = "#6C63FF",
    val deadline: LocalDate? = null,
) {
    val targetValue: Double? get() = targetText.replace(",", ".").toDoubleOrNull()
    val isValid: Boolean get() = title.isNotBlank() && (targetValue ?: 0.0) > 0.0
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(CreateGoalForm())
    private val _sheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<GoalsUiState> = goalRepository.observeActive()
        .map { goals ->
            GoalsUiState(
                isLoading = false,
                cards = goals.map { it.toCard() },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = GoalsUiState(),
        )

    val sheetVisible: StateFlow<Boolean> = _sheetVisible.asStateFlow()
    val form: StateFlow<CreateGoalForm> = _form.asStateFlow()

    fun openCreate() { _form.value = CreateGoalForm(); _sheetVisible.value = true }
    fun closeCreate() { _sheetVisible.value = false }

    fun setTitle(v: String) { _form.value = _form.value.copy(title = v) }
    fun setTarget(v: String) {
        val cleaned = v.filter { it.isDigit() || it == '.' || it == ',' }
        _form.value = _form.value.copy(targetText = cleaned)
    }
    fun setIcon(v: String) { _form.value = _form.value.copy(icon = v) }
    fun setColor(v: String) { _form.value = _form.value.copy(colorHex = v) }
    fun setDeadline(v: LocalDate?) { _form.value = _form.value.copy(deadline = v) }

    fun saveGoal() {
        val f = _form.value
        if (!f.isValid) return
        viewModelScope.launch {
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

    private fun Goal.toCard(): GoalCard {
        val today = LocalDate.now()
        val progress = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
        val remaining = (targetAmount - currentAmount).coerceAtLeast(0.0)
        val months = deadline?.let { ChronoUnit.MONTHS.between(today.withDayOfMonth(1), it.withDayOfMonth(1)).toInt() }
        val isOverdue = deadline?.isBefore(today) == true && remaining > 0
        val monthly = if (months != null && months > 0 && remaining > 0) {
            ceil(remaining / months)
        } else null
        return GoalCard(this, progress, remaining, months, monthly, isOverdue)
    }
}

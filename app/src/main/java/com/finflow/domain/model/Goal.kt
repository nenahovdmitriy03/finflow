package com.finflow.domain.model

import java.time.LocalDate

data class Goal(
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: LocalDate? = null,
    val icon: String = "🎯",
    val colorHex: String = "#6C63FF",
    val isArchived: Boolean = false,
) {
    val progress: Float
        get() = if (targetAmount <= 0) 0f
        else (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
}

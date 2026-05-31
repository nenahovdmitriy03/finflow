package com.finflow.domain.model

import java.time.LocalDate

enum class BudgetPeriod { MONTHLY, WEEKLY }

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val limitAmount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val start: LocalDate,
    val notify80: Boolean = true,
    val notify100: Boolean = true,
)

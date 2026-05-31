package com.finflow.presentation.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {

    private val rubFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
        maximumFractionDigits = 0
    }

    fun format(amount: Double, currency: String = "RUB"): String = when (currency) {
        "RUB" -> rubFormatter.format(amount)
        else -> String.format(Locale.US, "%.2f %s", amount, currency)
    }

    fun formatSigned(amount: Double, isIncome: Boolean, currency: String = "RUB"): String {
        val sign = if (isIncome) "+" else "\u2212"
        return sign + format(abs(amount), currency)
    }
}

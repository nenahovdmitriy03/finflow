package com.finflow.data.local.seed

import com.finflow.data.local.entity.CategoryEntity
import com.finflow.data.local.entity.WalletEntity

object DefaultData {

    val expenseCategories = listOf(
        CategoryEntity(name = "Еда", icon = "🍔", colorHex = "#FF6B6B", type = "EXPENSE"),
        CategoryEntity(name = "Транспорт", icon = "🚗", colorHex = "#4ECDC4", type = "EXPENSE"),
        CategoryEntity(name = "Жильё", icon = "🏠", colorHex = "#FFA07A", type = "EXPENSE"),
        CategoryEntity(name = "Развлечения", icon = "🎮", colorHex = "#A78BFA", type = "EXPENSE"),
        CategoryEntity(name = "Здоровье", icon = "💊", colorHex = "#34D399", type = "EXPENSE"),
        CategoryEntity(name = "Одежда", icon = "👕", colorHex = "#F472B6", type = "EXPENSE"),
        CategoryEntity(name = "Кафе", icon = "☕", colorHex = "#FCD34D", type = "EXPENSE"),
        CategoryEntity(name = "Подписки", icon = "📱", colorHex = "#60A5FA", type = "EXPENSE"),
        CategoryEntity(name = "Прочее", icon = "📦", colorHex = "#94A3B8", type = "EXPENSE"),
    )

    val incomeCategories = listOf(
        CategoryEntity(name = "Зарплата", icon = "💼", colorHex = "#2ECC71", type = "INCOME"),
        CategoryEntity(name = "Фриланс", icon = "💻", colorHex = "#6C63FF", type = "INCOME"),
        CategoryEntity(name = "Подарок", icon = "🎁", colorHex = "#F472B6", type = "INCOME"),
        CategoryEntity(name = "Инвестиции", icon = "📈", colorHex = "#34D399", type = "INCOME"),
        CategoryEntity(name = "Прочее", icon = "💰", colorHex = "#94A3B8", type = "INCOME"),
    )

    val defaultWallets = listOf(
        WalletEntity(name = "Наличные", kind = "CASH", initialBalance = 0.0),
        WalletEntity(name = "Карта", kind = "CARD", initialBalance = 0.0),
    )
}

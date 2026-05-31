package com.finflow.data.local.mapper

import com.finflow.data.local.entity.CategoryEntity
import com.finflow.data.local.entity.GoalEntity
import com.finflow.data.local.entity.TransactionEntity
import com.finflow.data.local.entity.WalletEntity
import com.finflow.domain.model.Category
import com.finflow.domain.model.Goal
import com.finflow.domain.model.Transaction
import com.finflow.domain.model.TransactionType
import com.finflow.domain.model.Wallet
import com.finflow.domain.model.WalletKind
import java.time.Instant
import java.time.LocalDate

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    categoryId = categoryId,
    walletId = walletId,
    date = Instant.ofEpochMilli(dateEpochMillis),
    note = note,
    receiptPhotoPath = receiptPhotoPath,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    walletId = walletId,
    dateEpochMillis = date.toEpochMilli(),
    note = note,
    receiptPhotoPath = receiptPhotoPath,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id, name = name, icon = icon, colorHex = colorHex,
    type = TransactionType.valueOf(type), isCustom = isCustom,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id, name = name, icon = icon, colorHex = colorHex,
    type = type.name, isCustom = isCustom,
)

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id, name = name, kind = WalletKind.valueOf(kind),
    initialBalance = initialBalance, currency = currency,
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id, name = name, kind = kind.name,
    initialBalance = initialBalance, currency = currency,
)

fun GoalEntity.toDomain(): Goal = Goal(
    id = id, title = title, targetAmount = targetAmount, currentAmount = currentAmount,
    deadline = deadlineEpochDay?.let { LocalDate.ofEpochDay(it) },
    icon = icon, colorHex = colorHex, isArchived = isArchived,
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id, title = title, targetAmount = targetAmount, currentAmount = currentAmount,
    deadlineEpochDay = deadline?.toEpochDay(),
    icon = icon, colorHex = colorHex, isArchived = isArchived,
)

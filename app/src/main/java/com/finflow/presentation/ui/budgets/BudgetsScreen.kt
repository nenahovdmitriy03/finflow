package com.finflow.presentation.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.domain.model.BudgetPeriod
import com.finflow.domain.model.Category
import com.finflow.presentation.util.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sheetVisible by viewModel.sheetVisible.collectAsState()
    val form by viewModel.form.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Бюджеты", fontWeight = FontWeight.SemiBold) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreate() },
                containerColor = MaterialTheme.colorScheme.primary,
            ) { Icon(Icons.Rounded.Add, contentDescription = "Добавить бюджет") }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.cards.isEmpty() -> EmptyState(modifier = Modifier.fillMaxSize().padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.cards, key = { it.budget.id }) { card ->
                    BudgetCardItem(card = card, onDelete = { viewModel.deleteBudget(card.budget.id) })
                }
            }
        }

        if (sheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeCreate() },
                sheetState = sheetState,
            ) {
                CreateBudgetSheet(
                    form = form,
                    availableCategories = state.availableCategories,
                    onCategory = viewModel::setCategory,
                    onLimit = viewModel::setLimit,
                    onPeriod = viewModel::setPeriod,
                    onSave = viewModel::saveBudget,
                    onCancel = viewModel::closeCreate,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📊", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))
        Text("Нет активных бюджетов", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Установи лимит по категории — мы предупредим, когда подойдёшь близко",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BudgetCardItem(card: BudgetCard, onDelete: () -> Unit) {
    val color = when {
        card.isOverLimit -> Color(0xFFFF6B6B)
        card.isWarning -> Color(0xFFFCD34D)
        else -> Color(0xFF2ECC71)
    }
    val categoryColor = card.category?.colorHex
        ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
        ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).background(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(card.category?.icon ?: "💰", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        card.category?.name ?: "Категория",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (card.budget.period == BudgetPeriod.MONTHLY) "Ежемесячно" else "Еженедельно",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { card.progress.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${MoneyFormatter.format(card.spent)} из ${MoneyFormatter.format(card.budget.limitAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    when {
                        card.isOverLimit -> "Превышено на ${MoneyFormatter.format(-card.remaining)}"
                        else -> "Осталось ${MoneyFormatter.format(card.remaining)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun CreateBudgetSheet(
    form: CreateBudgetForm,
    availableCategories: List<Category>,
    onCategory: (Long) -> Unit,
    onLimit: (String) -> Unit,
    onPeriod: (BudgetPeriod) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Новый бюджет", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))

        Text("Категория", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        if (availableCategories.isEmpty()) {
            Text(
                "Все категории расходов уже имеют бюджет",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableCategories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = form.categoryId == cat.id,
                        onClick = { onCategory(cat.id) },
                        label = { Text("${cat.icon} ${cat.name}") },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = form.limitText,
            onValueChange = onLimit,
            label = { Text("Лимит, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
        Text("Период", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = form.period == BudgetPeriod.MONTHLY,
                onClick = { onPeriod(BudgetPeriod.MONTHLY) },
                label = { Text("Месяц") },
            )
            FilterChip(
                selected = form.period == BudgetPeriod.WEEKLY,
                onClick = { onPeriod(BudgetPeriod.WEEKLY) },
                label = { Text("Неделя") },
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Отмена") }
            Spacer(Modifier.size(8.dp))
            TextButton(onClick = onSave, enabled = form.isValid) { Text("Сохранить") }
        }
    }
}

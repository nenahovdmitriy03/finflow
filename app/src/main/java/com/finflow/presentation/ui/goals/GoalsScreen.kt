package com.finflow.presentation.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.presentation.theme.Income
import com.finflow.presentation.theme.Primary
import com.finflow.presentation.util.MoneyFormatter

private val palette = listOf("#6C63FF", "#2ECC71", "#FF6B6B", "#4ECDC4", "#FCD34D", "#A78BFA", "#F472B6", "#60A5FA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val sheetVisible by viewModel.sheetVisible.collectAsState()
    val form by viewModel.form.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var contributionGoalId by rememberSaveable { mutableStateOf<Long?>(null) }
    var contributionText by rememberSaveable { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreate() },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Новая цель") },
                containerColor = MaterialTheme.colorScheme.primary,
            )
        }
    ) { padding ->
        if (state.cards.isEmpty() && !state.isLoading) {
            EmptyGoals(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("Цели", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                }
                items(state.cards, key = { it.goal.id }) { card ->
                    GoalCardView(
                        card = card,
                        onEdit = { viewModel.openEdit(card) },
                        onContribute = {
                            contributionGoalId = card.goal.id
                            contributionText = ""
                        },
                        onArchive = { viewModel.archiveGoal(card.goal.id) },
                        onDelete = { deleteId = card.goal.id },
                    )
                }
            }
        }

        if (sheetVisible) {
            ModalBottomSheet(onDismissRequest = { viewModel.closeSheet() }, sheetState = sheetState) {
                GoalSheet(
                    form = form,
                    onTitle = viewModel::setTitle,
                    onTarget = viewModel::setTarget,
                    onColor = viewModel::setColor,
                    onSave = viewModel::saveGoal,
                )
            }
        }

        contributionGoalId?.let { gid ->
            AlertDialog(
                onDismissRequest = { contributionGoalId = null },
                title = { Text("Пополнить цель") },
                text = {
                    OutlinedTextField(
                        value = contributionText,
                        onValueChange = { v -> contributionText = v.filter { it.isDigit() || it == '.' || it == ',' } },
                        label = { Text("Сумма, ₽") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val amount = contributionText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        viewModel.addContribution(gid, amount)
                        contributionGoalId = null
                    }) { Text("Добавить") }
                },
                dismissButton = { TextButton(onClick = { contributionGoalId = null }) { Text("Отмена") } },
            )
        }

        deleteId?.let { id ->
            AlertDialog(
                onDismissRequest = { deleteId = null },
                title = { Text("Удалить цель?") },
                text = { Text("Цель и история накоплений будут удалены. Это действие нельзя отменить.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteGoal(id); deleteId = null }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Отмена") } },
            )
        }
    }
}

@Composable
private fun GoalCardView(
    card: GoalCard,
    onEdit: () -> Unit,
    onContribute: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(card.goal.colorHex)) }.getOrNull() ?: Primary
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) { Text(card.goal.title.firstOrNull()?.uppercase() ?: "T", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color) }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(card.goal.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${MoneyFormatter.format(card.goal.currentAmount)} из ${MoneyFormatter.format(card.goal.targetAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Text("${(card.progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { card.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
            )
            Spacer(Modifier.height(8.dp))
            if (card.remaining > 0) {
                when {
                    card.monthlySuggestion != null && !card.isOverdue -> Text(
                        "Откладывай по ${MoneyFormatter.format(card.monthlySuggestion)} /мес",
                        color = Income, style = MaterialTheme.typography.bodyMedium,
                    )
                    card.isOverdue -> Text(
                        "Срок прошёл, цель не достигнута",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    else -> Text(
                        "Осталось накопить ${MoneyFormatter.format(card.remaining)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            } else {
                Text("Цель достигнута!", color = Income, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onContribute, colors = ButtonDefaults.buttonColors(containerColor = color)) {
                    Text("Пополнить")
                }
                TextButton(onClick = onArchive) { Text("В архив") }
                TextButton(onClick = onDelete) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalSheet(
    form: GoalForm,
    onTitle: (String) -> Unit,
    onTarget: (String) -> Unit,
    onColor: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            if (form.isEditing) "Редактировать цель" else "Новая цель",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = form.title, onValueChange = onTitle,
            label = { Text("Название") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = form.targetText, onValueChange = onTarget,
            label = { Text("Сумма цели, ₽") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text("Цвет", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(palette) { hex -> ColorDot(hex = hex, selected = form.colorHex == hex, onClick = { onColor(hex) }) }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSave, enabled = form.isValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
        ) { Text(if (form.isEditing) "Сохранить изменения" else "Создать цель") }
        Spacer(Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorDot(hex: String, selected: Boolean, onClick: () -> Unit) {
    val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull() ?: Primary
    Surface(onClick = onClick, shape = CircleShape, color = c) {
        Box(Modifier.size(if (selected) 40.dp else 32.dp).background(c))
    }
}

@Composable
private fun EmptyGoals(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Пока нет целей", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Создай первую — копить станет проще", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

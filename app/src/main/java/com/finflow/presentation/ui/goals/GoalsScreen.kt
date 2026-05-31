package com.finflow.presentation.ui.goals

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
private val icons = listOf("🎯", "💰", "🏖️", "🚗", "🏠", "💻", "🎓", "💍", "🎁", "📱", "✈️", "🛠️")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val sheetVisible by viewModel.sheetVisible.collectAsState()
    val form by viewModel.form.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var contributionGoalId by rememberSaveable { mutableStateOf<Long?>(null) }
    var contributionText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openCreate,
                containerColor = Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Новая цель") },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (state.cards.isEmpty() && !state.isLoading) {
            EmptyGoals(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            ) {
                item {
                    Text(
                        "Цели",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
                items(state.cards, key = { it.goal.id }) { card ->
                    GoalCardView(
                        card = card,
                        onContribute = { contributionGoalId = card.goal.id; contributionText = "" },
                        onArchive = { viewModel.archiveGoal(card.goal.id) },
                    )
                }
            }
        }
    }

    if (sheetVisible) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeCreate,
            sheetState = sheetState,
        ) {
            CreateGoalSheet(
                form = form,
                onTitle = viewModel::setTitle,
                onTarget = viewModel::setTarget,
                onIcon = viewModel::setIcon,
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
                    onValueChange = { contributionText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Сумма, ₽") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val v = contributionText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        viewModel.addContribution(gid, v)
                        contributionGoalId = null
                    },
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { contributionGoalId = null }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun GoalCardView(card: GoalCard, onContribute: () -> Unit, onArchive: () -> Unit) {
    val parsed = runCatching {
        Color(android.graphics.Color.parseColor(card.goal.colorHex))
    }.getOrDefault(Primary)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(parsed.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) { Text(card.goal.icon ?: "🎯", fontSize = 26.sp) }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(card.goal.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text(
                        MoneyFormatter.format(card.goal.currentAmount) + " из " + MoneyFormatter.format(card.goal.targetAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Text(
                    (card.progress * 100).toInt().toString() + "%",
                    fontWeight = FontWeight.Bold,
                    color = parsed,
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { card.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = parsed,
                trackColor = parsed.copy(alpha = 0.15f),
            )
            if (card.remaining > 0) {
                Spacer(Modifier.height(10.dp))
                if (card.monthlySuggestion != null && !card.isOverdue) {
                    Text(
                        "Откладывай по " + MoneyFormatter.format(card.monthlySuggestion) + " /мес",
                        color = Income,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    )
                } else if (card.isOverdue) {
                    Text("Срок прошёл, цель не достигнута", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                } else {
                    Text("Осталось накопить " + MoneyFormatter.format(card.remaining), fontSize = 13.sp)
                }
            } else {
                Spacer(Modifier.height(10.dp))
                Text("🎉 Цель достигнута!", color = Income, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onContribute,
                    colors = ButtonDefaults.buttonColors(containerColor = parsed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) { Text("Пополнить") }
                TextButton(onClick = onArchive) { Text("Архив") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGoalSheet(
    form: CreateGoalForm,
    onTitle: (String) -> Unit,
    onTarget: (String) -> Unit,
    onIcon: (String) -> Unit,
    onColor: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text("Новая цель", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = form.title,
            onValueChange = onTitle,
            label = { Text("Название") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = form.targetText,
            onValueChange = onTarget,
            label = { Text("Сумма цели, ₽") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Иконка", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(icons) { ic ->
                IconChip(emoji = ic, selected = form.icon == ic, onClick = { onIcon(ic) })
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Цвет", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(palette) { hex ->
                ColorDot(hex = hex, selected = form.colorHex == hex, onClick = { onColor(hex) })
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSave,
            enabled = form.isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(14.dp),
        ) { Text("Создать цель", fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconChip(emoji: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bg,
        modifier = Modifier.size(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorDot(hex: String, selected: Boolean, onClick: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Primary)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        modifier = Modifier.size(if (selected) 40.dp else 32.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(color))
    }
}

@Composable
private fun EmptyGoals(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎯", fontSize = 56.sp)
            Spacer(Modifier.height(10.dp))
            Text("Пока нет целей", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Создай первую — копить станет проще",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

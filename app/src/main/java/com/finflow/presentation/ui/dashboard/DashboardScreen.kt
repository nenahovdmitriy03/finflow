package com.finflow.presentation.ui.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.domain.model.TransactionType
import com.finflow.presentation.theme.Expense
import com.finflow.presentation.theme.Income
import com.finflow.presentation.theme.Primary
import com.finflow.presentation.theme.PrimaryVariant
import com.finflow.presentation.util.MoneyFormatter
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                containerColor = Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Добавить") },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item { BalanceHeader(state) }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text(
                    "Последние операции",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
            if (state.recentTransactions.isEmpty()) {
                item { EmptyState() }
            } else {
                items(state.recentTransactions, key = { it.transaction.id }) { tx ->
                    SwipeableTransactionRow(
                        item = tx,
                        onDelete = { viewModel.delete(tx.transaction.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(state: DashboardUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(colors = listOf(Primary, PrimaryVariant)))
            .padding(24.dp),
    ) {
        Column {
            Text("Текущий баланс", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = MoneyFormatter.format(state.balance),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MoneyChip("Доходы (мес.)", state.monthIncome, Icons.Rounded.ArrowDownward, Income)
                MoneyChip("Расходы (мес.)", state.monthExpense, Icons.Rounded.ArrowUpward, Expense)
            }
        }
    }
}

@Composable
private fun MoneyChip(label: String, amount: Double, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Spacer(Modifier.size(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            Text(MoneyFormatter.format(amount), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("ru"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionRow(item: TransactionUi, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Expense.copy(alpha = 0.85f))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = Color.White)
            }
        },
    ) {
        TransactionRow(item)
    }
}

@Composable
private fun TransactionRow(item: TransactionUi) {
    val isIncome = item.transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) Income else Expense
    val cat = item.category

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val bg = runCatching {
                Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#94A3B8"))
            }.getOrDefault(Color.Gray).copy(alpha = 0.18f)

            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Text(cat?.icon ?: "💸", fontSize = 22.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cat?.name ?: "Без категории",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    item.transaction.note ?: item.transaction.date.atZone(ZoneId.systemDefault()).format(timeFmt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text(
                MoneyFormatter.formatSigned(item.transaction.amount, isIncome),
                color = amountColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💸", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text("Пока нет операций", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Нажми «Добавить», чтобы записать первую",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

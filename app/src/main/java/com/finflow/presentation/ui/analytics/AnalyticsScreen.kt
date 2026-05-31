package com.finflow.presentation.ui.analytics

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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.presentation.theme.Expense
import com.finflow.presentation.theme.Income
import com.finflow.presentation.theme.Primary
import com.finflow.presentation.util.MoneyFormatter
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { MonthSwitcher(state, viewModel::previousMonth, viewModel::nextMonth) }
            item { Spacer(Modifier.height(8.dp)) }
            item { TotalsCard(state) }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                SectionTitle("Расходы по дням")
                DailyChart(state.dailyExpense.map { it.amount })
            }
            item { Spacer(Modifier.height(16.dp)) }
            item { SectionTitle("Расходы по категориям") }
            if (state.expenseByCategory.isEmpty()) {
                item { EmptyAnalytics() }
            } else {
                items(state.expenseByCategory, key = { it.category.id }) { slice ->
                    CategoryRow(slice)
                }
            }
        }
    }
}

@Composable
private fun MonthSwitcher(state: AnalyticsUiState, onPrev: () -> Unit, onNext: () -> Unit) {
    val fmt = remember { DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Rounded.ChevronLeft, null) }
        Text(
            state.month.atDay(1).format(fmt).replaceFirstChar { it.titlecase(Locale("ru")) },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onNext) { Icon(Icons.Rounded.ChevronRight, null) }
    }
}

@Composable
private fun TotalsCard(state: AnalyticsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            TotalColumn("Доходы", state.totalIncome, Income, Modifier.weight(1f))
            Spacer(Modifier.size(12.dp))
            TotalColumn("Расходы", state.totalExpense, Expense, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TotalColumn(label: String, amount: Double, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        Spacer(Modifier.size(4.dp))
        Text(
            MoneyFormatter.format(amount),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
private fun DailyChart(daily: List<Double>) {
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(daily) {
        if (daily.any { it > 0.0 }) {
            producer.runTransaction {
                columnSeries { series(daily) }
            }
        }
    }
    if (daily.none { it > 0.0 }) {
        EmptyAnalytics(text = "Нет расходов за этот месяц")
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                fill = fill(Primary),
                                thickness = 8.dp,
                                shape = CorneredShape.rounded(allPercent = 30),
                            )
                        )
                    ),
                    startAxis = rememberStartAxis(label = rememberAxisLabelComponent()),
                    bottomAxis = rememberBottomAxis(label = rememberAxisLabelComponent()),
                ),
                modelProducer = producer,
                scrollState = rememberVicoScrollState(),
            )
        }
    }
}

@Composable
private fun CategoryRow(slice: CategorySlice) {
    val parsed = runCatching {
        Color(android.graphics.Color.parseColor(slice.category.colorHex))
    }.getOrDefault(Primary)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(parsed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(slice.category.icon, fontSize = 18.sp)
                }
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(slice.category.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        (slice.share * 100).toInt().toString() + " %",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                    )
                }
                Text(MoneyFormatter.format(slice.amount), fontWeight = FontWeight.Bold, color = Expense)
            }
            Spacer(Modifier.size(8.dp))
            LinearProgressIndicator(
                progress = { slice.share.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = parsed,
                trackColor = parsed.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun EmptyAnalytics(text: String = "Пока нет данных") {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 40.sp)
            Spacer(Modifier.size(6.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

package com.finflow.presentation.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finflow.domain.model.Category
import com.finflow.domain.model.TransactionType
import com.finflow.domain.model.Wallet
import com.finflow.presentation.theme.Expense
import com.finflow.presentation.theme.Income
import com.finflow.presentation.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая операция", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Rounded.Close, contentDescription = "Закрыть")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        ) {
            TypeSelector(state.type, viewModel::setType)
            Spacer(Modifier.height(20.dp))
            AmountInput(state.amountText, viewModel::setAmount, state.type == TransactionType.INCOME)
            Spacer(Modifier.height(20.dp))
            Text("Категория", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.weight(1f, fill = true)) {
                CategoryGrid(state.categories, state.selectedCategoryId, viewModel::selectCategory)
            }
            if (state.wallets.size > 1) {
                Spacer(Modifier.height(12.dp))
                WalletPicker(state.wallets, state.selectedWalletId, viewModel::selectWallet)
            }
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                label = { Text("Заметка (необязательно)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = state.canSave && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(54.dp).padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    if (state.isSaving) "Сохранение..." else "Сохранить",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TypeSelector(current: TransactionType, onChange: (TransactionType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TypeChip("Расход", current == TransactionType.EXPENSE, Expense, Modifier.weight(1f)) {
            onChange(TransactionType.EXPENSE)
        }
        TypeChip("Доход", current == TransactionType.INCOME, Income, Modifier.weight(1f)) {
            onChange(TransactionType.INCOME)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeChip(
    label: String, selected: Boolean, accent: Color, modifier: Modifier, onClick: () -> Unit,
) {
    val bg = if (selected) accent else Color.Transparent
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier.height(40.dp),
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, color = fg, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AmountInput(text: String, onChange: (String) -> Unit, isIncome: Boolean) {
    val color = if (isIncome) Income else Expense
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        placeholder = {
            Text(
                "0",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        textStyle = MaterialTheme.typography.headlineLarge.copy(
            color = color,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
        suffix = { Text("₽", fontSize = 24.sp, fontWeight = FontWeight.SemiBold) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
private fun CategoryGrid(categories: List<Category>, selectedId: Long?, onSelect: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(categories, key = { it.id }) { cat ->
            CategoryCell(cat, selected = cat.id == selectedId, onClick = { onSelect(cat.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCell(category: Category, selected: Boolean, onClick: () -> Unit) {
    val parsed = runCatching {
        Color(android.graphics.Color.parseColor(category.colorHex))
    }.getOrDefault(Primary)
    val bg = if (selected) parsed.copy(alpha = 0.95f) else parsed.copy(alpha = 0.18f)
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = bg) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(category.icon, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                category.name,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletPicker(wallets: List<Wallet>, selectedId: Long?, onSelect: (Long) -> Unit) {
    Column {
        Text(
            "Кошелёк",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(wallets, key = { it.id }) { wallet ->
                FilterChip(
                    selected = wallet.id == selectedId,
                    onClick = { onSelect(wallet.id) },
                    label = { Text(wallet.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
    }
}

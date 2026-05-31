package com.finflow.presentation.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finflow.domain.model.Category
import com.finflow.domain.model.Transaction
import com.finflow.domain.model.TransactionType
import com.finflow.domain.model.Wallet
import com.finflow.domain.repository.CategoryRepository
import com.finflow.domain.repository.TransactionRepository
import com.finflow.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: Long? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
) {
    val amountValue: Double?
        get() = amountText.replace(",", ".").toDoubleOrNull()

    val canSave: Boolean
        get() = (amountValue ?: 0.0) > 0.0 && selectedCategoryId != null && selectedWalletId != null
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionUiState())
    val state: StateFlow<AddTransactionUiState> = _state.asStateFlow()

    init { loadFor(TransactionType.EXPENSE) }

    fun setType(type: TransactionType) {
        if (type == _state.value.type) return
        _state.update { it.copy(type = type, selectedCategoryId = null) }
        loadFor(type)
    }

    fun setAmount(value: String) {
        val cleaned = value.filter { it.isDigit() || it == '.' || it == ',' }
        _state.update { it.copy(amountText = cleaned) }
    }

    fun setNote(value: String) = _state.update { it.copy(note = value) }
    fun selectCategory(id: Long) = _state.update { it.copy(selectedCategoryId = id) }
    fun selectWallet(id: Long) = _state.update { it.copy(selectedWalletId = id) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                transactionRepository.add(
                    Transaction(
                        amount = s.amountValue ?: 0.0,
                        type = s.type,
                        categoryId = s.selectedCategoryId!!,
                        walletId = s.selectedWalletId!!,
                        date = Instant.now(),
                        note = s.note.ifBlank { null },
                    )
                )
                onDone()
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message ?: "Ошибка сохранения", isSaving = false) }
            }
        }
    }

    private fun loadFor(type: TransactionType) {
        viewModelScope.launch {
            val cats = categoryRepository.observeByType(type).first()
            val wallets = walletRepository.observeAll().first()
            _state.update {
                it.copy(
                    categories = cats,
                    wallets = wallets,
                    selectedWalletId = it.selectedWalletId ?: wallets.firstOrNull()?.id,
                )
            }
        }
    }
}

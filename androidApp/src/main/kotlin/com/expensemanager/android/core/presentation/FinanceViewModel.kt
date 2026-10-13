package com.expensemanager.android.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.expensemanager.android.core.data.FinanceRepository
import com.expensemanager.android.core.model.TransactionDraft
import com.expensemanager.android.core.model.FinanceTransaction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FinanceEvent {
    data object TransactionSaved : FinanceEvent
    data class Error(val message: String) : FinanceEvent
}

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {
    val transactions: StateFlow<List<FinanceTransaction>> = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _events = MutableSharedFlow<FinanceEvent>()
    val events = _events.asSharedFlow()

    fun saveTransaction(draft: TransactionDraft) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            runCatching { repository.addTransaction(draft) }
                .onSuccess { _events.emit(FinanceEvent.TransactionSaved) }
                .onFailure { _events.emit(FinanceEvent.Error("Could not save this transaction. Try again.")) }
            _isSaving.value = false
        }
    }
}

class FinanceViewModelFactory(
    private val repository: FinanceRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FinanceViewModel::class.java))
        return FinanceViewModel(repository) as T
    }
}

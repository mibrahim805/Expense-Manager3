package com.expensemanager.android.core.data

import com.expensemanager.android.core.data.local.TransactionDao
import com.expensemanager.android.core.data.local.TransactionEntity
import com.expensemanager.android.core.model.Account
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.TransactionDraft
import com.expensemanager.android.core.model.TransactionType
import com.expensemanager.android.core.model.validateTransactionDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(private val transactionDao: TransactionDao) {
    val transactions: Flow<List<FinanceTransaction>> = transactionDao.observeAll().map { records ->
        records.map { record ->
            FinanceTransaction(
                id = record.id,
                type = TransactionType.valueOf(record.type),
                amountMinor = record.amountMinor,
                currencyCode = record.currencyCode,
                category = FinanceCategory.fromKey(record.categoryKey),
                description = record.description,
                account = Account(record.accountId, record.accountName, record.currencyCode),
                destinationAccount = record.destinationAccountId?.let {
                    Account(it, record.destinationAccountName.orEmpty(), record.currencyCode)
                },
                timestampMillis = record.timestampMillis,
            )
        }
    }

    suspend fun addTransaction(draft: TransactionDraft): Boolean {
        val validation = validateTransactionDraft(draft)
        require(validation.isValid) { validation.message ?: "Invalid transaction." }
        return transactionDao.insert(draft.toEntity()) != -1L
    }

    suspend fun updateTransaction(id: Long, draft: TransactionDraft): Boolean {
        val validation = validateTransactionDraft(draft)
        require(validation.isValid) { validation.message ?: "Invalid transaction." }
        return transactionDao.update(draft.toEntity(id)) > 0
    }

    suspend fun deleteTransaction(id: Long): Boolean = transactionDao.deleteById(id) > 0

    private fun TransactionDraft.toEntity(id: Long = 0L) = TransactionEntity(
        id = id,
        type = type.name,
        amountMinor = amountMinor,
        currencyCode = currencyCode,
        categoryKey = category.key,
        description = description,
        accountId = account.id,
        accountName = account.name,
        destinationAccountId = destinationAccount?.id,
        destinationAccountName = destinationAccount?.name,
        timestampMillis = timestampMillis,
        requestId = requestId,
    )
}

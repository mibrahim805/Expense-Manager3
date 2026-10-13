package com.expensemanager.android.core.data

import com.expensemanager.android.core.data.local.TransactionDao
import com.expensemanager.android.core.data.local.TransactionEntity
import com.expensemanager.android.core.model.Account
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.TransactionDraft
import com.expensemanager.android.core.model.TransactionType
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

    suspend fun addTransaction(draft: TransactionDraft) {
        transactionDao.insert(
            TransactionEntity(
                type = draft.type.name,
                amountMinor = draft.amountMinor,
                currencyCode = draft.currencyCode,
                categoryKey = draft.category.key,
                description = draft.description,
                accountId = draft.account.id,
                accountName = draft.account.name,
                destinationAccountId = draft.destinationAccount?.id,
                destinationAccountName = draft.destinationAccount?.name,
                timestampMillis = draft.timestampMillis,
            )
        )
    }
}

package com.expensemanager.android.core.data

import com.expensemanager.android.core.data.local.BudgetDao
import com.expensemanager.android.core.data.local.BudgetEntity
import com.expensemanager.android.core.data.local.TransactionDao
import com.expensemanager.android.core.data.local.TransactionEntity
import com.expensemanager.android.core.model.Account
import com.expensemanager.android.core.model.CategoryBudget
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.TransactionDraft
import com.expensemanager.android.core.model.TransactionType
import com.expensemanager.android.core.model.validateTransactionDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
) {
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

    val budgets: Flow<List<CategoryBudget>> = budgetDao.observeAll().map { records ->
        records.map { record ->
            CategoryBudget(
                category = FinanceCategory.fromKey(record.categoryKey),
                limitMinor = record.limitMinor,
                currencyCode = record.currencyCode,
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

    suspend fun setBudget(category: FinanceCategory, limitMinor: Long) {
        require(category in FinanceCategory.forType(TransactionType.EXPENSE)) { "Choose an expense category." }
        require(limitMinor > 0L) { "Enter a budget greater than zero." }
        budgetDao.upsert(BudgetEntity(category.key, limitMinor))
    }

    suspend fun deleteBudget(category: FinanceCategory): Boolean =
        budgetDao.deleteByCategory(category.key) > 0

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

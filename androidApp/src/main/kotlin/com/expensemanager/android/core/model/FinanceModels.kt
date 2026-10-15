package com.expensemanager.android.core.model

import java.util.UUID

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
}

enum class FinanceCategory(
    val key: String,
    val label: String,
    val iconName: String,
) {
    FOOD("food", "Food", "restaurant"),
    TRANSPORT("transport", "Transport", "directions_car"),
    BILLS("bills", "Bills", "receipt_long"),
    SHOPPING("shopping", "Shopping", "shopping_bag"),
    HEALTH("health", "Health", "favorite"),
    EDUCATION("education", "Education", "school"),
    ENTERTAINMENT("entertainment", "Entertainment", "movie"),
    SALARY("salary", "Salary", "payments"),
    FREELANCE("freelance", "Freelance", "work"),
    OTHER("other", "Other", "more_horiz"),
    TRANSFER("transfer", "Transfer", "swap_horiz"),
    ;

    companion object {
        fun fromKey(key: String?): FinanceCategory =
            entries.firstOrNull { it.key == key } ?: OTHER

        fun forType(type: TransactionType): List<FinanceCategory> = when (type) {
            TransactionType.EXPENSE -> listOf(FOOD, TRANSPORT, BILLS, SHOPPING, HEALTH, EDUCATION, ENTERTAINMENT, OTHER)
            TransactionType.INCOME -> listOf(SALARY, FREELANCE, OTHER)
            TransactionType.TRANSFER -> listOf(TRANSFER)
        }
    }
}

data class Account(
    val id: String,
    val name: String,
    val currencyCode: String,
)

data class FinanceTransaction(
    val id: Long,
    val type: TransactionType,
    val amountMinor: Long,
    val currencyCode: String,
    val category: FinanceCategory,
    val description: String,
    val account: Account,
    val destinationAccount: Account? = null,
    val timestampMillis: Long,
)

data class CategoryBudget(
    val category: FinanceCategory,
    val limitMinor: Long,
    val currencyCode: String = "PKR",
)

data class TransactionDraft(
    val type: TransactionType,
    val amountMinor: Long,
    val currencyCode: String,
    val category: FinanceCategory,
    val description: String,
    val account: Account,
    val destinationAccount: Account? = null,
    val timestampMillis: Long,
    val requestId: String = UUID.randomUUID().toString(),
)

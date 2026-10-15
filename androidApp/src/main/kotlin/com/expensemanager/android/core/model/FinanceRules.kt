package com.expensemanager.android.core.model

data class TransactionValidationResult(
    val isValid: Boolean,
    val message: String? = null,
) {
    companion object {
        val Valid = TransactionValidationResult(isValid = true)
    }
}

fun validateTransactionDraft(draft: TransactionDraft): TransactionValidationResult {
    if (draft.amountMinor <= 0L) {
        return TransactionValidationResult(false, "Enter an amount greater than zero.")
    }
    if (draft.currencyCode.isBlank() || draft.account.currencyCode != draft.currencyCode) {
        return TransactionValidationResult(false, "Choose a valid account and currency.")
    }
    if (draft.category !in FinanceCategory.forType(draft.type)) {
        return TransactionValidationResult(false, "Choose a valid category for this transaction.")
    }
    if (draft.account.id.isBlank()) {
        return TransactionValidationResult(false, "Choose an account.")
    }
    if (draft.timestampMillis <= 0L) {
        return TransactionValidationResult(false, "Choose a valid date and time.")
    }

    return when (draft.type) {
        TransactionType.TRANSFER -> {
            val destination = draft.destinationAccount
                ?: return TransactionValidationResult(false, "Choose a destination account.")
            when {
                destination.id.isBlank() -> TransactionValidationResult(false, "Choose a destination account.")
                destination.id == draft.account.id -> TransactionValidationResult(false, "Source and destination accounts must differ.")
                destination.currencyCode != draft.currencyCode -> TransactionValidationResult(
                    false,
                    "Cross-currency transfers require an exchange rate.",
                )
                else -> TransactionValidationResult.Valid
            }
        }

        TransactionType.EXPENSE, TransactionType.INCOME -> {
            if (draft.destinationAccount != null) {
                TransactionValidationResult(false, "This transaction cannot have a destination account.")
            } else {
                TransactionValidationResult.Valid
            }
        }
    }
}

fun calculateIncome(transactions: List<FinanceTransaction>): Long =
    transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }

fun calculateExpenses(transactions: List<FinanceTransaction>): Long =
    transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }

fun calculateTotalBalance(transactions: List<FinanceTransaction>): Long =
    calculateIncome(transactions) - calculateExpenses(transactions)

fun calculateAccountBalance(transactions: List<FinanceTransaction>, accountId: String): Long =
    transactions.sumOf { transaction ->
        when (transaction.type) {
            TransactionType.INCOME -> if (transaction.account.id == accountId) transaction.amountMinor else 0L
            TransactionType.EXPENSE -> if (transaction.account.id == accountId) -transaction.amountMinor else 0L
            TransactionType.TRANSFER -> when {
                transaction.account.id == accountId -> -transaction.amountMinor
                transaction.destinationAccount?.id == accountId -> transaction.amountMinor
                else -> 0L
            }
        }
    }

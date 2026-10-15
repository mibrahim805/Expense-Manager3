package com.expensemanager.android.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceRulesTest {
    private val cash = Account("cash", "Cash", "PKR")
    private val bank = Account("bank", "Bank", "PKR")

    @Test
    fun accountBalancesApplyExpenseIncomeAndTransferCorrectly() {
        val transactions = listOf(
            FinanceTransaction(1, TransactionType.INCOME, 100_000, "PKR", FinanceCategory.SALARY, "Pay", cash, timestampMillis = 1),
            FinanceTransaction(2, TransactionType.EXPENSE, 2_500, "PKR", FinanceCategory.FOOD, "Lunch", cash, timestampMillis = 2),
            FinanceTransaction(3, TransactionType.TRANSFER, 20_000, "PKR", FinanceCategory.TRANSFER, "Move", cash, bank, 3),
        )

        assertEquals(77_500L, calculateAccountBalance(transactions, "cash"))
        assertEquals(20_000L, calculateAccountBalance(transactions, "bank"))
        assertEquals(100_000L, calculateIncome(transactions))
        assertEquals(2_500L, calculateExpenses(transactions))
        assertEquals(97_500L, calculateTotalBalance(transactions))
    }

    @Test
    fun transferRequiresDifferentAccounts() {
        val sameAccount = TransactionDraft(
            type = TransactionType.TRANSFER,
            amountMinor = 1_000,
            currencyCode = "PKR",
            category = FinanceCategory.TRANSFER,
            description = "",
            account = cash,
            destinationAccount = cash,
            timestampMillis = 1,
        )

        val result = validateTransactionDraft(sameAccount)

        assertFalse(result.isValid)
        assertTrue(result.message!!.contains("differ"))
    }

    @Test
    fun invalidCategoryAndAmountAreRejectedOutsideTheUi() {
        val draft = TransactionDraft(
            type = TransactionType.EXPENSE,
            amountMinor = 0,
            currencyCode = "PKR",
            category = FinanceCategory.SALARY,
            description = "",
            account = cash,
            timestampMillis = 1,
        )

        val result = validateTransactionDraft(draft)

        assertFalse(result.isValid)
        assertTrue(result.message!!.contains("amount"))
    }
}

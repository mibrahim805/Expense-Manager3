package com.expensemanager.android.core.model

import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinanceInsightsTest {
    private val zone = ZoneId.of("UTC")
    private val cash = Account("cash", "Cash", "PKR")

    @Test
    fun calculatesCurrentMonthSummaryAndCategoryShares() {
        val month = YearMonth.of(2026, 10)
        val transactions = listOf(
            transaction(1, TransactionType.INCOME, 100_000, FinanceCategory.SALARY, 2026, 10, 1),
            transaction(2, TransactionType.EXPENSE, 20_000, FinanceCategory.FOOD, 2026, 10, 2),
            transaction(3, TransactionType.EXPENSE, 30_000, FinanceCategory.BILLS, 2026, 10, 2),
            transaction(4, TransactionType.EXPENSE, 25_000, FinanceCategory.FOOD, 2026, 9, 3),
        )

        val insights = calculateFinanceInsights(transactions, month, zone)

        assertEquals(100_000L, insights.current.incomeMinor)
        assertEquals(50_000L, insights.current.expenseMinor)
        assertEquals(50_000L, insights.current.netMinor)
        assertEquals(50, insights.savingsRatePercent)
        assertEquals(100, insights.expenseChangePercent)
        assertEquals(FinanceCategory.BILLS, insights.categorySpending.first().category)
        assertEquals(0.6f, insights.categorySpending.first().share, 0.001f)
        assertEquals(50_000L, insights.busiestSpendingDay?.amountMinor)
        assertEquals(30_000L, insights.largestExpense?.amountMinor)
        assertEquals(6, insights.sixMonthHistory.size)
    }

    @Test
    fun omitsRatesWhenThereIsNoIncomeOrPreviousSpending() {
        val insights = calculateFinanceInsights(
            transactions = listOf(
                transaction(1, TransactionType.EXPENSE, 5_000, FinanceCategory.FOOD, 2026, 10, 1)
            ),
            currentMonth = YearMonth.of(2026, 10),
            zoneId = zone,
        )

        assertNull(insights.savingsRatePercent)
        assertNull(insights.expenseChangePercent)
    }

    private fun transaction(
        id: Long,
        type: TransactionType,
        amountMinor: Long,
        category: FinanceCategory,
        year: Int,
        month: Int,
        day: Int,
    ) = FinanceTransaction(
        id = id,
        type = type,
        amountMinor = amountMinor,
        currencyCode = "PKR",
        category = category,
        description = category.label,
        account = cash,
        timestampMillis = LocalDateTime.of(year, month, day, 12, 0).atZone(zone).toInstant().toEpochMilli(),
    )
}

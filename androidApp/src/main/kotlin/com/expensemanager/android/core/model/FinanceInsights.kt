package com.expensemanager.android.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.roundToInt

data class CategorySpending(
    val category: FinanceCategory,
    val amountMinor: Long,
    val share: Float,
)

data class MonthlyFinanceSnapshot(
    val month: YearMonth,
    val incomeMinor: Long,
    val expenseMinor: Long,
) {
    val netMinor: Long get() = incomeMinor - expenseMinor
}

data class SpendingDay(
    val date: LocalDate,
    val amountMinor: Long,
)

data class FinanceInsights(
    val current: MonthlyFinanceSnapshot,
    val savingsRatePercent: Int?,
    val expenseChangePercent: Int?,
    val categorySpending: List<CategorySpending>,
    val sixMonthHistory: List<MonthlyFinanceSnapshot>,
    val busiestSpendingDay: SpendingDay?,
    val largestExpense: FinanceTransaction?,
)

fun calculateFinanceInsights(
    transactions: List<FinanceTransaction>,
    currentMonth: YearMonth = YearMonth.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): FinanceInsights {
    val history = (5 downTo 0).map { offset ->
        monthlySnapshot(transactions, currentMonth.minusMonths(offset.toLong()), zoneId)
    }
    val current = history.last()
    val previous = monthlySnapshot(transactions, currentMonth.minusMonths(1), zoneId)
    val currentExpenses = transactions.filter { transaction ->
        transaction.type == TransactionType.EXPENSE && transaction.yearMonth(zoneId) == currentMonth
    }
    val categoryTotals = currentExpenses
        .groupBy { it.category }
        .mapValues { (_, values) -> values.sumOf { it.amountMinor } }
        .toList()
        .sortedByDescending { (_, amount) -> amount }
    val categorySpending = categoryTotals.map { (category, amount) ->
        CategorySpending(
            category = category,
            amountMinor = amount,
            share = if (current.expenseMinor > 0L) amount.toFloat() / current.expenseMinor else 0f,
        )
    }
    val busiestDay = currentExpenses
        .groupBy { it.localDate(zoneId) }
        .map { (date, values) -> SpendingDay(date, values.sumOf { it.amountMinor }) }
        .maxByOrNull { it.amountMinor }

    return FinanceInsights(
        current = current,
        savingsRatePercent = if (current.incomeMinor > 0L) {
            (current.netMinor.toDouble() / current.incomeMinor.toDouble() * 100).roundToInt()
        } else {
            null
        },
        expenseChangePercent = if (previous.expenseMinor > 0L) {
            ((current.expenseMinor - previous.expenseMinor).toDouble() / previous.expenseMinor.toDouble() * 100).roundToInt()
        } else {
            null
        },
        categorySpending = categorySpending,
        sixMonthHistory = history,
        busiestSpendingDay = busiestDay,
        largestExpense = currentExpenses.maxByOrNull { it.amountMinor },
    )
}

private fun monthlySnapshot(
    transactions: List<FinanceTransaction>,
    month: YearMonth,
    zoneId: ZoneId,
): MonthlyFinanceSnapshot {
    val monthTransactions = transactions.filter { it.yearMonth(zoneId) == month }
    return MonthlyFinanceSnapshot(
        month = month,
        incomeMinor = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
        expenseMinor = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
    )
}

private fun FinanceTransaction.yearMonth(zoneId: ZoneId): YearMonth =
    YearMonth.from(Instant.ofEpochMilli(timestampMillis).atZone(zoneId))

private fun FinanceTransaction.localDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(timestampMillis).atZone(zoneId).toLocalDate()

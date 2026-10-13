package com.expensemanager.android.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.FinanceColors
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.TransactionType
import com.expensemanager.android.core.model.formatMoney
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@Composable
fun HomeScreen(
    transactions: List<FinanceTransaction>,
    onAddExpense: () -> Unit,
    onViewAll: () -> Unit,
) {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
    val balance = income - expenses

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            GreetingHeader()
        }
        item {
            BalanceSummaryCard(balance = balance, income = income, expenses = expenses)
        }
        item {
            SpendingOverviewCard(transactions = transactions)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Recent activity", style = MaterialTheme.typography.titleLarge)
                Text(
                    "View all",
                    color = FinanceColors.PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(onClick = onViewAll),
                )
            }
        }
        if (transactions.isEmpty()) {
            item { EmptyDashboard(onAddExpense) }
        } else {
            items(transactions.take(5), key = { it.id }) { transaction ->
                TransactionRow(transaction)
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun GreetingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Good to see you", color = FinanceColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
            Text("My finances", style = MaterialTheme.typography.headlineSmall)
        }
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Open profile and settings" },
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FinanceColors.ElevatedSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text("ME", color = FinanceColors.PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BalanceSummaryCard(balance: Long, income: Long, expenses: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FinanceColors.PrimarySurface),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(listOf(ColorTokens.balanceStart, ColorTokens.balanceEnd))
                )
                .padding(20.dp),
        ) {
            Text("Total balance", color = FinanceColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
            Text(
                formatMoney(balance, withSign = true),
                style = MaterialTheme.typography.displaySmall,
                color = FinanceColors.PrimaryText,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ArrowDownward,
                    label = "Income",
                    amount = income,
                    color = FinanceColors.Positive,
                )
                Box(Modifier.width(1.dp).height(42.dp).background(FinanceColors.CardBorder))
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ArrowUpward,
                    label = "Expenses",
                    amount = expenses,
                    color = FinanceColors.Negative,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(modifier = modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            Text(label, color = FinanceColors.SecondaryText, style = MaterialTheme.typography.labelMedium)
        }
        Text(formatMoney(amount), color = FinanceColors.PrimaryText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SpendingOverviewCard(transactions: List<FinanceTransaction>) {
    val days = remember { (0..6).map { LocalDate.now().minusDays((6 - it).toLong()) } }
    val values = remember(transactions) {
        days.map { day ->
            transactions.filter { transaction ->
                transaction.type == TransactionType.EXPENSE && transaction.localDate() == day
            }.sumOf { it.amountMinor }
        }
    }
    var selectedIndex by remember { mutableIntStateOf(values.lastIndex.coerceAtLeast(0)) }
    val maxValue = max(values.maxOrNull() ?: 0L, 1L)
    val selectedValue = values.getOrNull(selectedIndex) ?: 0L

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Spending overview", style = MaterialTheme.typography.titleLarge)
            Text("Week", color = FinanceColors.PrimaryBlue, style = MaterialTheme.typography.bodyMedium)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FinanceColors.PrimarySurface),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("This week", color = FinanceColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
                Text(formatMoney(selectedValue), style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    values.forEachIndexed { index, value ->
                        val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0.08f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { selectedIndex = index }
                                .semantics {
                                    contentDescription = "${days[index].dayOfWeek.name.lowercase()} ${formatMoney(value)}"
                                },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxSize(fraction)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (index == selectedIndex) FinanceColors.PrimaryBlue else FinanceColors.ElevatedSurface),
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEach { day ->
                        Text(
                            day.dayOfWeek.name.take(1),
                            color = FinanceColors.MutedText,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: FinanceTransaction) {
    val signedAmount = when (transaction.type) {
        TransactionType.INCOME -> transaction.amountMinor
        else -> -transaction.amountMinor
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FinanceColors.ElevatedSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(categoryIcon(transaction.category), contentDescription = transaction.category.label, tint = FinanceColors.PrimaryBlue)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(transaction.description.ifBlank { transaction.category.label }, fontWeight = FontWeight.SemiBold)
            Text(
                "${transaction.category.label} · ${transaction.localDate().format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))}",
                color = FinanceColors.MutedText,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            formatMoney(signedAmount, withSign = true),
            color = if (transaction.type == TransactionType.INCOME) FinanceColors.Positive else FinanceColors.Negative,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyDashboard(onAddExpense: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FinanceColors.PrimarySurface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = FinanceColors.PrimaryBlue, modifier = Modifier.size(28.dp))
            Text("Add your first expense", style = MaterialTheme.typography.titleLarge)
            Text("Track your spending and understand your finances at a glance.", color = FinanceColors.SecondaryText)
            androidx.compose.material3.TextButton(onClick = onAddExpense) { Text("Add expense") }
        }
    }
}

private fun FinanceTransaction.localDate(): LocalDate =
    Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()

private fun categoryIcon(category: FinanceCategory): ImageVector = when (category) {
    FinanceCategory.FOOD -> Icons.Default.Restaurant
    FinanceCategory.TRANSPORT -> Icons.Default.DirectionsCar
    FinanceCategory.BILLS -> Icons.Default.ReceiptLong
    FinanceCategory.SHOPPING -> Icons.Default.ShoppingBag
    FinanceCategory.HEALTH -> Icons.Default.Favorite
    FinanceCategory.EDUCATION -> Icons.Default.School
    FinanceCategory.ENTERTAINMENT -> Icons.Default.Movie
    FinanceCategory.SALARY -> Icons.Default.Payments
    FinanceCategory.FREELANCE -> Icons.Default.Work
    FinanceCategory.TRANSFER -> Icons.Default.SwapHoriz
    FinanceCategory.OTHER -> Icons.Default.MoreHoriz
}

private object ColorTokens {
    val balanceStart = FinanceColors.ElevatedSurface
    val balanceEnd = FinanceColors.PrimarySurface
}

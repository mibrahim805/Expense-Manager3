package com.expensemanager.android.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.FinanceThemeColors
import com.expensemanager.android.core.designsystem.financeColors
import com.expensemanager.android.core.model.CategorySpending
import com.expensemanager.android.core.model.FinanceInsights
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.MonthlyFinanceSnapshot
import com.expensemanager.android.core.model.calculateFinanceInsights
import com.expensemanager.android.core.model.formatMoney
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun InsightsScreen(transactions: List<FinanceTransaction>) {
    val colors = financeColors()
    val month = YearMonth.now()
    val insights = remember(transactions, month) { calculateFinanceInsights(transactions, month) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.appBackground),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Insights", color = colors.primaryText, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Your money story for ${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item { CashFlowCard(insights, colors) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                InsightMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    amount = insights.current.incomeMinor,
                    icon = Icons.Default.ArrowUpward,
                    accent = colors.income,
                    colors = colors,
                )
                InsightMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Spending",
                    amount = insights.current.expenseMinor,
                    icon = Icons.Default.ArrowDownward,
                    accent = colors.expense,
                    colors = colors,
                )
            }
        }
        item { SixMonthTrendCard(insights.sixMonthHistory, colors) }
        item { CategoryBreakdownCard(insights.categorySpending, insights.current.expenseMinor, colors) }
        item { HighlightsCard(insights, colors) }
    }
}

@Composable
private fun CashFlowCard(insights: FinanceInsights, colors: FinanceThemeColors) {
    val net = insights.current.netMinor
    val netColor = if (net >= 0L) colors.income else colors.expense
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(colors.balanceStart, colors.balanceEnd)))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net cash flow", color = colors.secondaryText, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.Savings, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            }
            Text(
                formatMoney(net.absoluteValue),
                color = netColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (net >= 0L) "You kept more than you spent" else "Spending is above income",
                color = colors.secondaryText,
                style = MaterialTheme.typography.bodyMedium,
            )
            val savingsRate = insights.savingsRatePercent
            if (savingsRate != null) {
                Text(
                    "Savings rate: $savingsRate%",
                    color = colors.primaryText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun InsightMetricCard(
    modifier: Modifier,
    label: String,
    amount: Long,
    icon: ImageVector,
    accent: Color,
    colors: FinanceThemeColors,
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(colors.surface)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = colors.appBackground, modifier = Modifier.size(17.dp))
                }
                Text(label, color = colors.secondaryText, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                formatMoney(amount),
                color = colors.primaryText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SixMonthTrendCard(history: List<MonthlyFinanceSnapshot>, colors: FinanceThemeColors) {
    val maxValue = history.maxOfOrNull { maxOf(it.incomeMinor, it.expenseMinor) }?.coerceAtLeast(1L) ?: 1L
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(colors.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Six-month trend", color = colors.primaryText, style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot("Income", colors.income, colors)
                    LegendDot("Spent", colors.expense, colors)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(116.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                history.forEach { snapshot ->
                    val incomeHeight = (6f + snapshot.incomeMinor.toFloat() / maxValue * 72f).dp
                    val expenseHeight = (6f + snapshot.expenseMinor.toFloat() / maxValue * 72f).dp
                    Column(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(Modifier.width(8.dp).height(incomeHeight).clip(RoundedCornerShape(4.dp)).background(colors.income))
                            Box(Modifier.width(8.dp).height(expenseHeight).clip(RoundedCornerShape(4.dp)).background(colors.expense))
                        }
                        Text(
                            snapshot.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                            color = colors.secondaryText,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 7.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, colors: FinanceThemeColors) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(label, color = colors.secondaryText, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CategoryBreakdownCard(
    categories: List<CategorySpending>,
    totalExpenses: Long,
    colors: FinanceThemeColors,
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(colors.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text("Spending by category", color = colors.primaryText, style = MaterialTheme.typography.titleLarge)
            if (categories.isEmpty()) {
                Text("Add an expense to see your category breakdown.", color = colors.secondaryText)
            } else {
                categories.take(6).forEachIndexed { index, category ->
                    val accent = colors.categoryTints[category.category.ordinal.coerceIn(0, colors.categoryTints.lastIndex)]
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(category.category.label, color = colors.primaryText, fontWeight = FontWeight.SemiBold)
                            Text(formatMoney(category.amountMinor), color = colors.secondaryText, style = MaterialTheme.typography.bodyMedium)
                        }
                        LinearProgressIndicator(
                            progress = { category.share.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).semantics {
                                contentDescription = "${category.category.label}, ${(category.share * 100).toInt()} percent of spending"
                            },
                            color = accent,
                            trackColor = colors.elevatedSurface,
                        )
                    }
                    if (index < categories.take(6).lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.55f)))
                    }
                }
                Text(
                    "Total ${formatMoney(totalExpenses)}",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun HighlightsCard(insights: FinanceInsights, colors: FinanceThemeColors) {
    val change = insights.expenseChangePercent
    val trendText = when {
        change == null -> "More history needed"
        change > 0 -> "$change% more than last month"
        change < 0 -> "${change.absoluteValue}% less than last month"
        else -> "Same as last month"
    }
    val topCategory = insights.categorySpending.firstOrNull()
    val busiestDay = insights.busiestSpendingDay
    val largestExpense = insights.largestExpense

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(colors.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Highlights", color = colors.primaryText, style = MaterialTheme.typography.titleLarge)
            HighlightRow(Icons.Default.ArrowDownward, "Spending trend", trendText, colors.expense, colors)
            HighlightRow(
                Icons.Default.Category,
                "Top category",
                topCategory?.let { "${it.category.label} · ${formatMoney(it.amountMinor)}" } ?: "No spending yet",
                colors.primary,
                colors,
            )
            HighlightRow(
                Icons.Default.CalendarToday,
                "Highest-spend day",
                busiestDay?.let { "${it.date.format(DateTimeFormatter.ofPattern("dd MMM"))} · ${formatMoney(it.amountMinor)}" }
                    ?: "No spending yet",
                colors.warning,
                colors,
            )
            HighlightRow(
                Icons.Default.ReceiptLong,
                "Largest expense",
                largestExpense?.let {
                    "${it.description.ifBlank { it.category.label }} · ${formatMoney(it.amountMinor)}"
                } ?: "No spending yet",
                colors.secondaryText,
                colors,
            )
        }
    }
}

@Composable
private fun HighlightRow(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color,
    colors: FinanceThemeColors,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = colors.secondaryText, style = MaterialTheme.typography.labelMedium)
            Text(value, color = colors.primaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

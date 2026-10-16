package com.expensemanager.android.feature.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.FinanceThemeColors
import com.expensemanager.android.core.designsystem.financeColors
import com.expensemanager.android.core.model.CategoryBudget
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.TransactionType
import com.expensemanager.android.core.model.formatAmountForInput
import com.expensemanager.android.core.model.formatMoney
import com.expensemanager.android.core.model.maskedMoney
import com.expensemanager.android.core.model.parseAmountToMinorUnits
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BudgetsScreen(
    transactions: List<FinanceTransaction>,
    budgets: List<CategoryBudget>,
    amountsVisible: Boolean,
    onToggleAmountsVisibility: () -> Unit,
    onSetBudget: (FinanceCategory, Long) -> Unit,
    onDeleteBudget: (FinanceCategory) -> Unit,
) {
    val colors = financeColors()
    val month = YearMonth.now()
    val expenseCategories = FinanceCategory.forType(TransactionType.EXPENSE)
    val monthExpenses = remember(transactions, month) {
        transactions.filter { transaction ->
            transaction.type == TransactionType.EXPENSE && transaction.yearMonth() == month
        }
    }
    val spendingByCategory = remember(monthExpenses) {
        monthExpenses.groupBy { it.category }.mapValues { (_, values) -> values.sumOf { it.amountMinor } }
    }
    val budgetsByCategory = budgets.associateBy { it.category }
    var editingCategory by remember { mutableStateOf<FinanceCategory?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.appBackground),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Budgets", color = colors.primaryText, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Plan and track ${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                        color = colors.secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                IconButton(
                    onClick = onToggleAmountsVisibility,
                    modifier = Modifier.semantics {
                        contentDescription = if (amountsVisible) "Hide amounts" else "Show amounts"
                    },
                ) {
                    Icon(
                        if (amountsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                    )
                }
            }
        }
        item {
            BudgetSummaryCard(
                budgets = budgets,
                spendingByCategory = spendingByCategory,
                colors = colors,
                amountsVisible = amountsVisible,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Category limits", color = colors.primaryText, style = MaterialTheme.typography.titleLarge)
                Text("Monthly", color = colors.secondaryText, style = MaterialTheme.typography.labelMedium)
            }
        }
        items(expenseCategories, key = { it.key }) { category ->
            BudgetCategoryCard(
                category = category,
                budget = budgetsByCategory[category],
                spentMinor = spendingByCategory[category] ?: 0L,
                colors = colors,
                amountsVisible = amountsVisible,
                onEdit = { editingCategory = category },
                onDelete = { onDeleteBudget(category) },
            )
        }
    }

    editingCategory?.let { category ->
        BudgetEditorDialog(
            category = category,
            existingBudget = budgetsByCategory[category],
            onDismiss = { editingCategory = null },
            onSave = { limitMinor ->
                onSetBudget(category, limitMinor)
                editingCategory = null
            },
        )
    }
}

@Composable
private fun BudgetSummaryCard(
    budgets: List<CategoryBudget>,
    spendingByCategory: Map<FinanceCategory, Long>,
    colors: FinanceThemeColors,
    amountsVisible: Boolean,
) {
    val totalLimit = budgets.sumOf { it.limitMinor }
    val totalSpent = budgets.sumOf { budget -> spendingByCategory[budget.category] ?: 0L }
    val remaining = (totalLimit - totalSpent).coerceAtLeast(0L)
    val progress = if (totalLimit > 0L) (totalSpent.toFloat() / totalLimit).coerceIn(0f, 1f) else 0f
    val progressColor = budgetProgressColor(totalSpent, totalLimit, colors)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Monthly plan", color = colors.secondaryText, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (totalLimit <= 0L) "No limits set" else if (amountsVisible) formatMoney(totalLimit) else maskedMoney(),
                color = colors.primaryText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = progressColor,
                trackColor = colors.elevatedSurface,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryValue("Spent", if (amountsVisible) formatMoney(totalSpent) else maskedMoney(), colors)
                SummaryValue(
                    "Remaining",
                    if (amountsVisible) formatMoney(remaining) else maskedMoney(),
                    colors,
                    Alignment.End,
                )
            }
        }
    }
}

@Composable
private fun SummaryValue(
    label: String,
    value: String,
    colors: FinanceThemeColors,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = alignment) {
        Text(label, color = colors.secondaryText, style = MaterialTheme.typography.labelMedium)
        Text(value, color = colors.primaryText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BudgetCategoryCard(
    category: FinanceCategory,
    budget: CategoryBudget?,
    spentMinor: Long,
    colors: FinanceThemeColors,
    amountsVisible: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val limit = budget?.limitMinor ?: 0L
    val progress = if (limit > 0L) (spentMinor.toFloat() / limit).coerceIn(0f, 1f) else 0f
    val progressColor = budgetProgressColor(spentMinor, limit, colors)
    val status = if (!amountsVisible) {
        "Amount hidden"
    } else when {
        budget == null -> "No budget set"
        spentMinor > limit -> "${formatMoney(spentMinor - limit)} over"
        else -> "${formatMoney(limit - spentMinor)} left"
    }

    Card(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = if (amountsVisible) {
                "${category.label} budget, spent ${formatMoney(spentMinor)}, $status"
            } else {
                "${category.label} budget, amount hidden"
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(colors.categoryTints[category.ordinal]),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(categoryIcon(category), contentDescription = null, tint = colors.primaryText, modifier = Modifier.size(21.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(category.label, color = colors.primaryText, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (!amountsVisible) {
                            maskedMoney()
                        } else if (budget == null) {
                            formatMoney(spentMinor) + " spent"
                        } else {
                            "${formatMoney(spentMinor)} of ${formatMoney(limit)}"
                        },
                        color = colors.secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                TextButton(onClick = onEdit) { Text(if (budget == null) "Set" else "Edit") }
                if (budget != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.semantics { contentDescription = "Delete ${category.label} budget" }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.secondaryText)
                    }
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = progressColor,
                trackColor = colors.elevatedSurface,
            )
            Text(status, color = progressColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BudgetEditorDialog(
    category: FinanceCategory,
    existingBudget: CategoryBudget?,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var amountText by remember(category, existingBudget?.limitMinor) {
        mutableStateOf(existingBudget?.let { formatAmountForInput(it.limitMinor) }.orEmpty())
    }
    val amountMinor = parseAmountToMinorUnits(amountText)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${if (existingBudget == null) "Set" else "Edit"} ${category.label} budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Set the monthly spending limit for this category.")
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value -> amountText = value.filter { it.isDigit() || it == '.' || it == ',' } },
                    label = { Text("Monthly limit") },
                    prefix = { Text("Rs ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { amountMinor?.let(onSave) }, enabled = amountMinor != null) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun budgetProgressColor(spent: Long, limit: Long, colors: FinanceThemeColors): Color = when {
    limit <= 0L -> colors.mutedText
    spent > limit -> colors.expense
    spent * 100 >= limit * 80 -> colors.warning
    else -> colors.primary
}

private fun FinanceTransaction.yearMonth(): YearMonth =
    YearMonth.from(Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()))

private fun categoryIcon(category: FinanceCategory): ImageVector = when (category) {
    FinanceCategory.FOOD -> Icons.Default.Restaurant
    FinanceCategory.TRANSPORT -> Icons.Default.DirectionsCar
    FinanceCategory.BILLS -> Icons.Default.ReceiptLong
    FinanceCategory.SHOPPING -> Icons.Default.ShoppingBag
    FinanceCategory.HEALTH -> Icons.Default.Favorite
    FinanceCategory.EDUCATION -> Icons.Default.School
    FinanceCategory.ENTERTAINMENT -> Icons.Default.Movie
    else -> Icons.Default.LocalGasStation
}

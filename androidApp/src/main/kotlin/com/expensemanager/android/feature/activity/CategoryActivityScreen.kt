@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.expensemanager.android.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.FinanceThemeColors
import com.expensemanager.android.core.designsystem.financeColors
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.FinanceTransaction
import com.expensemanager.android.core.model.TransactionType
import com.expensemanager.android.core.model.formatMoney
import com.expensemanager.android.core.model.maskedMoney
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CategoryActivityScreen(
    category: FinanceCategory,
    transactions: List<FinanceTransaction>,
    amountsVisible: Boolean,
    onToggleAmountsVisibility: () -> Unit,
    onBack: () -> Unit,
    onEditTransaction: (FinanceTransaction) -> Unit,
    onDeleteTransaction: (FinanceTransaction) -> Unit,
) {
    val colors = financeColors()
    val categoryTransactions = remember(transactions, category) {
        transactions
            .filter { it.type == TransactionType.EXPENSE && it.category == category }
            .sortedByDescending { it.timestampMillis }
    }
    val groupedTransactions = remember(categoryTransactions) {
        categoryTransactions.groupBy { it.localDate() }.toSortedMap(compareByDescending { it })
    }
    val totalSpent = categoryTransactions.sumOf { it.amountMinor }
    var selectedTransaction by remember { mutableStateOf<FinanceTransaction?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.appBackground),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            CategoryActivityHeader(
                category = category,
                amountsVisible = amountsVisible,
                onToggleAmountsVisibility = onToggleAmountsVisibility,
                onBack = onBack,
            )
        }
        item {
            CategoryActivitySummary(
                category = category,
                totalSpent = totalSpent,
                transactionCount = categoryTransactions.size,
                amountsVisible = amountsVisible,
                colors = colors,
            )
        }
        if (categoryTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("No ${category.label.lowercase()} expenses yet", fontWeight = FontWeight.SemiBold)
                        Text("New ${category.label.lowercase()} transactions will appear here.", color = colors.secondaryText)
                    }
                }
            }
        } else {
            groupedTransactions.forEach { (date, dateTransactions) ->
                item(key = "date-$date") {
                    Text(
                        dateLabel(date),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        color = colors.secondaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(dateTransactions, key = { it.id }) { transaction ->
                    CategoryTransactionCard(
                        transaction = transaction,
                        amountsVisible = amountsVisible,
                        colors = colors,
                        onClick = { selectedTransaction = transaction },
                    )
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        CategoryTransactionDetails(
            transaction = transaction,
            amountsVisible = amountsVisible,
            onDismiss = { selectedTransaction = null },
            onEdit = {
                selectedTransaction = null
                onEditTransaction(transaction)
            },
            onDelete = {
                selectedTransaction = null
                onDeleteTransaction(transaction)
            },
        )
    }
}

@Composable
private fun CategoryActivityHeader(
    category: FinanceCategory,
    amountsVisible: Boolean,
    onToggleAmountsVisibility: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("${category.label} Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Every ${category.label.lowercase()} expense", color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun CategoryActivitySummary(
    category: FinanceCategory,
    totalSpent: Long,
    transactionCount: Int,
    amountsVisible: Boolean,
    colors: FinanceThemeColors,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(colors.categoryTints[category.ordinal]),
                contentAlignment = Alignment.Center,
            ) {
                Icon(categoryIcon(category), contentDescription = null, modifier = Modifier.size(25.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Total spent", color = colors.secondaryText, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (amountsVisible) formatMoney(totalSpent) else maskedMoney(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "$transactionCount transaction${if (transactionCount == 1) "" else "s"}",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun CategoryTransactionCard(
    transaction: FinanceTransaction,
    amountsVisible: Boolean,
    colors: FinanceThemeColors,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(colors.categoryTints[transaction.category.ordinal]),
                contentAlignment = Alignment.Center,
            ) {
                Icon(categoryIcon(transaction.category), contentDescription = null, modifier = Modifier.size(21.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    transaction.description.ifBlank { transaction.category.label },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${transaction.account.name} · ${transaction.localTime()}",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text(
                if (amountsVisible) formatMoney(transaction.amountMinor, transaction.currencyCode) else maskedMoney(),
                color = colors.expense,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategoryTransactionDetails(
    transaction: FinanceTransaction,
    amountsVisible: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = financeColors()
    var confirmDelete by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(transaction.description.ifBlank { transaction.category.label }, style = MaterialTheme.typography.headlineSmall)
            Text(
                if (amountsVisible) formatMoney(transaction.amountMinor, transaction.currencyCode) else maskedMoney(),
                color = colors.expense,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            DetailRow("Category", transaction.category.label, colors)
            DetailRow("Account", transaction.account.name, colors)
            DetailRow("Date", transaction.localDateTimeText(), colors)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit")
                }
                Button(onClick = { confirmDelete = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete transaction?") },
            text = { Text("This expense will be removed from ${transaction.category.label} activity.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, colors: FinanceThemeColors) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = colors.secondaryText)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun FinanceTransaction.localDate(): LocalDate =
    Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()

private fun FinanceTransaction.localTime(): String =
    Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

private fun FinanceTransaction.localDateTimeText(): String =
    Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault()))

private fun dateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
    }
}

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

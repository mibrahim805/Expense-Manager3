package com.expensemanager.android.feature.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.FinanceColors
import com.expensemanager.android.core.model.Account
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.TransactionDraft
import com.expensemanager.android.core.model.TransactionType
import com.expensemanager.android.core.model.parseAmountToMinorUnits
import java.text.DateFormat
import java.util.Date

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun AddTransactionScreen(
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: (TransactionDraft) -> Unit,
) {
    var typeName by rememberSaveable { mutableStateOf(TransactionType.EXPENSE.name) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryKey by rememberSaveable { mutableStateOf(FinanceCategory.FOOD.key) }
    var timestampMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    var showAccountSheet by rememberSaveable { mutableStateOf(false) }
    var showAllCategories by rememberSaveable { mutableStateOf(false) }
    var selectingDestination by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var accountId by rememberSaveable { mutableStateOf("cash") }
    var destinationAccountId by rememberSaveable { mutableStateOf("bank") }
    val selectedType = TransactionType.valueOf(typeName)
    val categories = FinanceCategory.forType(selectedType)
    val selectedCategory = categories.firstOrNull { it.key == categoryKey } ?: categories.first()
    val account = accountForId(accountId)
    val destinationAccount = accountForId(destinationAccountId)
    val amountMinor = parseAmountToMinorUnits(amountText)
    val accountsAreValid = selectedType != TransactionType.TRANSFER || account.id != destinationAccount.id
    val hasUnsavedChanges = amountText.isNotBlank() || description.isNotBlank() || attemptedSave
    val context = LocalContext.current

    fun requestBack() {
        if (hasUnsavedChanges && !isSaving) showDiscardDialog = true else onBack()
    }

    BackHandler(onBack = ::requestBack)

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = ::requestBack,
                modifier = Modifier.semantics { contentDescription = "Close transaction editor" },
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            Text("Add transaction", style = MaterialTheme.typography.headlineSmall)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, transactionType ->
                    SegmentedButton(
                        selected = transactionType == selectedType,
                        onClick = {
                            typeName = transactionType.name
                            categoryKey = FinanceCategory.forType(transactionType).first().key
                            showAllCategories = false
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TransactionType.entries.size),
                        icon = {},
                    ) {
                        Text(transactionType.label())
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Enter amount", color = FinanceColors.SecondaryText, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' || char == ',' } },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textStyle = MaterialTheme.typography.displaySmall,
                    placeholder = { Text("0.00", style = MaterialTheme.typography.displaySmall, color = FinanceColors.MutedText) },
                    prefix = { Text("Rs ", style = MaterialTheme.typography.titleLarge) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = attemptedSave && amountMinor == null,
                    supportingText = if (attemptedSave && amountMinor == null) {
                        { Text("Enter a valid amount greater than zero.") }
                    } else null,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val visibleCategories = if (showAllCategories) categories else categories.take(4)
                    visibleCategories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { categoryKey = category.key },
                            label = { Text(category.label) },
                        )
                    }
                    if (categories.size > 4 && !showAllCategories) {
                        FilterChip(
                            selected = false,
                            onClick = { showAllCategories = true },
                            label = { Text("See all") },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (selectedType == TransactionType.TRANSFER) "From account" else "Paid from", style = MaterialTheme.typography.titleLarge)
                AccountField(account) {
                    selectingDestination = false
                    showAccountSheet = true
                }
                if (selectedType == TransactionType.TRANSFER) {
                    Text("To account", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                    AccountField(destinationAccount) {
                        selectingDestination = true
                        showAccountSheet = true
                    }
                    if (!accountsAreValid && attemptedSave) {
                        Text("Choose two different accounts.", color = FinanceColors.Negative, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Date and time", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(
                    onClick = {
                        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestampMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(year, month, day, hour, minute)
                                        timestampMillis = calendar.timeInMillis
                                    },
                                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                    calendar.get(java.util.Calendar.MINUTE),
                                    false,
                                ).show()
                            },
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH),
                            calendar.get(java.util.Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestampMillis)))
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                attemptedSave = true
                if (amountMinor != null && accountsAreValid) {
                    onSave(
                        TransactionDraft(
                            type = selectedType,
                            amountMinor = amountMinor,
                            currencyCode = account.currencyCode,
                            category = selectedCategory,
                            description = description.trim(),
                            account = account,
                            destinationAccount = if (selectedType == TransactionType.TRANSFER) destinationAccount else null,
                            timestampMillis = timestampMillis,
                        )
                    )
                }
            },
            enabled = amountMinor != null && accountsAreValid && !isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (isSaving) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.width(20.dp).height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Save ${selectedType.label().lowercase()}")
            }
        }
    }

    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose account", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = {
                    if (selectingDestination) destinationAccountId = "cash" else accountId = "cash"
                    showAccountSheet = false
                }) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Cash · PKR")
                }
                TextButton(onClick = {
                    if (selectingDestination) destinationAccountId = "bank" else accountId = "bank"
                    showAccountSheet = false
                }) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Bank · PKR")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard transaction?") },
            text = { Text("Your entered details will be lost.") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            },
        )
    }
}

private fun TransactionType.label(): String = when (this) {
    TransactionType.EXPENSE -> "Expense"
    TransactionType.INCOME -> "Income"
    TransactionType.TRANSFER -> "Transfer"
}

@Composable
private fun AccountField(account: Account, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(account.name)
            Text(account.currencyCode, color = FinanceColors.MutedText, style = MaterialTheme.typography.labelMedium)
        }
        Icon(Icons.Default.ExpandMore, contentDescription = "Choose account")
    }
}

private fun accountForId(id: String): Account = when (id) {
    "bank" -> Account("bank", "Bank", "PKR")
    else -> Account("cash", "Cash", "PKR")
}

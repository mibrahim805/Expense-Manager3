package com.expensemanager.android.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        androidx.room.Index(value = ["timestampMillis"]),
        androidx.room.Index(value = ["type"]),
        androidx.room.Index(value = ["requestId"], unique = true),
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryKey: String,
    val description: String,
    val accountId: String,
    val accountName: String,
    val destinationAccountId: String? = null,
    val destinationAccountName: String? = null,
    val timestampMillis: Long,
    val requestId: String? = null,
    @androidx.room.ColumnInfo(defaultValue = "'local'")
    val ownerId: String = FinanceDatabase.LOCAL_OWNER_ID,
)

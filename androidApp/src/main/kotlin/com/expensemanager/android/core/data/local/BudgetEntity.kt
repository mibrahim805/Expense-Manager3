package com.expensemanager.android.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [androidx.room.Index(value = ["ownerId", "categoryKey"], unique = true)],
)
data class BudgetEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    @androidx.room.ColumnInfo(defaultValue = "'local'") val ownerId: String = FinanceDatabase.LOCAL_OWNER_ID,
    val categoryKey: String,
    val limitMinor: Long,
    val currencyCode: String = "PKR",
)

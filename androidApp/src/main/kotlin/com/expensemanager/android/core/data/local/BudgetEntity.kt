package com.expensemanager.android.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val categoryKey: String,
    val limitMinor: Long,
    val currencyCode: String = "PKR",
)

package com.expensemanager.android

import android.app.Application
import com.expensemanager.android.core.data.FinanceRepository
import com.expensemanager.android.core.data.local.FinanceDatabase
import com.expensemanager.android.core.data.preferences.ThemePreferences

class ExpenseManagerApplication : Application() {
    private val database by lazy { FinanceDatabase.getInstance(this) }
    val financeRepository by lazy { FinanceRepository(database.transactionDao()) }
    val themePreferences by lazy { ThemePreferences(this) }
}

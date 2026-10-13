package com.expensemanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensemanager.android.core.designsystem.FinanceTheme
import com.expensemanager.android.core.presentation.FinanceViewModel
import com.expensemanager.android.core.presentation.FinanceViewModelFactory
import com.expensemanager.android.ui.ExpenseManagerApp

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as ExpenseManagerApplication
        setContent {
            FinanceTheme {
                val viewModel: FinanceViewModel = viewModel(
                    factory = FinanceViewModelFactory(application.financeRepository),
                )
                ExpenseManagerApp(viewModel)
            }
        }
    }
}

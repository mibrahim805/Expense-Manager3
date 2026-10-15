package com.expensemanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensemanager.android.core.designsystem.FinanceTheme
import com.expensemanager.android.core.designsystem.FinanceThemeMode
import com.expensemanager.android.core.presentation.FinanceViewModel
import com.expensemanager.android.core.presentation.FinanceViewModelFactory
import com.expensemanager.android.ui.ExpenseManagerApp
import kotlinx.coroutines.launch

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        val application = application as ExpenseManagerApplication
        setContent {
            val themeMode by application.themePreferences.theme.collectAsStateWithLifecycle(FinanceThemeMode.MIDNIGHT)
            FinanceTheme(themeMode) {
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = themeMode == FinanceThemeMode.ALMOND
                        isAppearanceLightNavigationBars = themeMode == FinanceThemeMode.ALMOND
                    }
                }
                val viewModel: FinanceViewModel = viewModel(
                    factory = FinanceViewModelFactory(application.financeRepository),
                )
                ExpenseManagerApp(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onThemeChange = { selectedTheme ->
                        lifecycleScope.launch { application.themePreferences.setTheme(selectedTheme) }
                    },
                )
            }
        }
    }
}

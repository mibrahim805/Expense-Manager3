package com.expensemanager.android

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensemanager.android.core.designsystem.FinanceTheme
import com.expensemanager.android.core.designsystem.FinanceThemeMode
import com.expensemanager.android.core.auth.PasswordResetLink
import com.expensemanager.android.feature.auth.AuthScreen
import com.expensemanager.android.core.presentation.FinanceViewModel
import com.expensemanager.android.core.presentation.FinanceViewModelFactory
import com.expensemanager.android.ui.ExpenseManagerApp
import kotlinx.coroutines.launch

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    private val applicationModel: ExpenseManagerApplication by lazy { application as ExpenseManagerApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        handlePasswordResetLink(intent?.data)
        val application = applicationModel
        setContent {
            val themeMode by application.themePreferences.theme.collectAsStateWithLifecycle(FinanceThemeMode.MIDNIGHT)
            val session by application.authRepository.session.collectAsStateWithLifecycle()
            val isRestoring by application.authRepository.isRestoring.collectAsStateWithLifecycle()
            val resetLink by application.authRepository.resetLink.collectAsStateWithLifecycle()
            FinanceTheme(themeMode) {
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = themeMode == FinanceThemeMode.ALMOND
                        isAppearanceLightNavigationBars = themeMode == FinanceThemeMode.ALMOND
                    }
                }
                LaunchedEffect(Unit) { application.authRepository.restoreSession() }
                when {
                    isRestoring -> AuthLoadingScreen()
                    session == null -> AuthScreen(application.authRepository, resetLink)
                    else -> {
                        val authenticatedUser = session!!.user
                        var isOwnerReady by remember(authenticatedUser.id) { mutableStateOf(false) }
                        LaunchedEffect(authenticatedUser.id) {
                            application.financeRepository.setOwner(authenticatedUser.id.toString())
                            isOwnerReady = true
                        }
                        if (!isOwnerReady) {
                            AuthLoadingScreen()
                        } else {
                            val viewModel: FinanceViewModel = viewModel(
                                key = "finance-${authenticatedUser.id}",
                                factory = FinanceViewModelFactory(application.financeRepository),
                            )
                            ExpenseManagerApp(
                                viewModel = viewModel,
                                themeMode = themeMode,
                                currentUser = authenticatedUser,
                                onLogout = { lifecycleScope.launch { runCatching { application.authRepository.logout() } } },
                                onThemeChange = { selectedTheme ->
                                    lifecycleScope.launch { application.themePreferences.setTheme(selectedTheme) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePasswordResetLink(intent.data)
    }

    private fun handlePasswordResetLink(uri: Uri?) {
        if (uri?.scheme != "rupeyawise" || uri.host != "auth" || uri.path != "/reset") return
        val uid = uri.getQueryParameter("uid") ?: return
        val token = uri.getQueryParameter("token") ?: return
        applicationModel.authRepository.openPasswordReset(PasswordResetLink(uid, token))
    }
}

@Composable
private fun AuthLoadingScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(com.expensemanager.android.core.designsystem.financeColors().appBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

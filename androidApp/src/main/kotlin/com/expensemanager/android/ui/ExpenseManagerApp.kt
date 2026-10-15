package com.expensemanager.android.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.expensemanager.android.core.designsystem.FinanceThemeMode
import com.expensemanager.android.core.presentation.FinanceEvent
import com.expensemanager.android.core.presentation.FinanceViewModel
import com.expensemanager.android.feature.home.HomeScreen
import com.expensemanager.android.feature.activity.ActivityScreen
import com.expensemanager.android.feature.transactions.AddTransactionScreen
import com.expensemanager.android.feature.settings.SettingsScreen

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination("home", "Home", Icons.Default.Home),
    TopLevelDestination("activity", "Activity", Icons.Default.ReceiptLong),
    TopLevelDestination("budgets", "Budgets", Icons.Default.PieChart),
    TopLevelDestination("insights", "Insights", Icons.Default.Analytics),
)

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun ExpenseManagerApp(
    viewModel: FinanceViewModel,
    themeMode: FinanceThemeMode,
    onThemeChange: (FinanceThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isFullscreen = currentRoute == "add" || currentRoute == "settings" || currentRoute?.startsWith("edit/") == true
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FinanceEvent.TransactionSaved -> {
                    navController.popBackStack()
                    snackbarHostState.showSnackbar("Transaction saved")
                }
                FinanceEvent.TransactionUpdated -> {
                    navController.popBackStack()
                    snackbarHostState.showSnackbar("Transaction updated")
                }
                FinanceEvent.TransactionDeleted -> snackbarHostState.showSnackbar("Transaction deleted")
                is FinanceEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isFullscreen) {
                NavigationBar(containerColor = colors.surface) {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                indicatorColor = colors.surfaceVariant,
                                unselectedIconColor = colors.onSurfaceVariant,
                                unselectedTextColor = colors.onSurfaceVariant,
                            ),
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isFullscreen) {
                FloatingActionButton(
                    onClick = { navController.navigate("add") },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    modifier = Modifier.semantics { contentDescription = "Add transaction" },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = if (isFullscreen) {
                Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            } else {
                Modifier.padding(paddingValues)
            },
        ) {
            composable("home") {
                HomeScreen(
                    transactions = transactions,
                    onAddExpense = { navController.navigate("add") },
                    onViewAll = { navController.navigate("activity") },
                    onOpenInsights = { navController.navigate("insights") },
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable("add") {
                AddTransactionScreen(
                    isSaving = isSaving,
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveTransaction,
                )
            }
            composable("edit/{transactionId}") { entry ->
                val transactionId = entry.arguments?.getString("transactionId")?.toLongOrNull()
                val transaction = transactions.firstOrNull { it.id == transactionId }
                if (transaction != null) {
                    AddTransactionScreen(
                        isSaving = isSaving,
                        existingTransaction = transaction,
                        onBack = { navController.popBackStack() },
                        onSave = { draft -> viewModel.updateTransaction(transaction.id, draft) },
                    )
                } else {
                    PlaceholderScreen("Transaction unavailable", "This transaction could not be found.")
                }
            }
            composable("settings") {
                SettingsScreen(
                    selectedTheme = themeMode,
                    onBack = { navController.popBackStack() },
                    onThemeSelected = onThemeChange,
                )
            }
            composable("activity") {
                ActivityScreen(
                    transactions = transactions,
                    onEditTransaction = { transaction -> navController.navigate("edit/${transaction.id}") },
                    onDeleteTransaction = { transaction -> viewModel.deleteTransaction(transaction.id) },
                )
            }
            composable("budgets") { PlaceholderScreen("Budgets", "Budget planning is coming next.") }
            composable("insights") { PlaceholderScreen("Insights", "Financial insights are coming next.") }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, message: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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
import com.expensemanager.android.core.designsystem.FinanceColors
import com.expensemanager.android.core.presentation.FinanceEvent
import com.expensemanager.android.core.presentation.FinanceViewModel
import com.expensemanager.android.feature.home.HomeScreen
import com.expensemanager.android.feature.transactions.AddTransactionScreen

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
fun ExpenseManagerApp(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isEditor = currentRoute == "add"

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FinanceEvent.TransactionSaved -> {
                    navController.popBackStack()
                    snackbarHostState.showSnackbar("Transaction saved")
                }
                is FinanceEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FinanceColors.AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isEditor) {
                NavigationBar(containerColor = FinanceColors.PrimarySurface) {
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
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isEditor) {
                FloatingActionButton(
                    onClick = { navController.navigate("add") },
                    containerColor = FinanceColors.PrimaryBlue,
                    contentColor = FinanceColors.PrimaryText,
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
            modifier = if (isEditor) {
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
                )
            }
            composable("add") {
                AddTransactionScreen(
                    isSaving = isSaving,
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::saveTransaction,
                )
            }
            composable("activity") { PlaceholderScreen("Activity", "Your complete transaction history will appear here.") }
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
            Text(message, color = FinanceColors.SecondaryText)
        }
    }
}

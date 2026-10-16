package com.expensemanager.android.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.financeColors
import com.expensemanager.android.core.update.AppUpdateInfo
import com.expensemanager.android.core.update.CurrentRelease

@Composable
fun WhatsNewScreen(onBack: () -> Unit) {
    val colors = financeColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.appBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to settings")
            }
            Text("What's new", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = colors.primary)
                Text(
                    "RupeyaWise ${CurrentRelease.versionName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Your money stays private, and your spending history is easier to explore.",
                    color = colors.secondaryText,
                )
                ReleaseNotes(CurrentRelease.notes)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CurrentReleaseDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text("Welcome to RupeyaWise ${CurrentRelease.versionName}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Here's what's new:")
                ReleaseNotes(CurrentRelease.notes)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Continue") }
        },
    )
}

@Composable
fun UpdateAvailableDialog(
    update: AppUpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text("Update available") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(update.title, fontWeight = FontWeight.SemiBold)
                Text("Version ${update.versionName}")
                ReleaseNotes(update.notes)
                Text(
                    "The APK will open in your browser. Install it over your current app to keep your data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = onDownload) { Text("Download update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        },
    )
}

@Composable
private fun ReleaseNotes(notes: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        notes.forEach { note ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(note, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

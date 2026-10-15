package com.expensemanager.android.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensemanager.android.core.designsystem.FinanceThemeMode
import com.expensemanager.android.core.designsystem.financeColors
import com.expensemanager.android.core.designsystem.financeThemeColors

@Composable
fun SettingsScreen(
    selectedTheme: FinanceThemeMode,
    onBack: () -> Unit,
    onThemeSelected: (FinanceThemeMode) -> Unit,
) {
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
            IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Back to dashboard" }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        Text("Appearance", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
        Text(
            "Choose how RupeyaWise looks on this device.",
            color = colors.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
        )
        ThemeOption(
            title = "Midnight Blue",
            description = "Premium dark finance theme",
            mode = FinanceThemeMode.MIDNIGHT,
            selected = selectedTheme == FinanceThemeMode.MIDNIGHT,
            preview = financeThemeColors(FinanceThemeMode.MIDNIGHT).appBackground,
            iconTint = financeThemeColors(FinanceThemeMode.MIDNIGHT).primary,
            onClick = { onThemeSelected(FinanceThemeMode.MIDNIGHT) },
        )
        Spacer(Modifier.height(10.dp))
        ThemeOption(
            title = "Almond Cream",
            description = "Warm, elegant light theme",
            mode = FinanceThemeMode.ALMOND,
            selected = selectedTheme == FinanceThemeMode.ALMOND,
            preview = financeThemeColors(FinanceThemeMode.ALMOND).appBackground,
            iconTint = financeThemeColors(FinanceThemeMode.ALMOND).primary,
            onClick = { onThemeSelected(FinanceThemeMode.ALMOND) },
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    description: String,
    mode: FinanceThemeMode,
    selected: Boolean,
    preview: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    val colors = financeColors()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$title theme${if (selected) ", selected" else ""}" },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) colors.primary else colors.border),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = if (mode == FinanceThemeMode.MIDNIGHT) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, color = colors.secondaryText, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(preview),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (selected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = iconTint, modifier = Modifier.size(18.dp))
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

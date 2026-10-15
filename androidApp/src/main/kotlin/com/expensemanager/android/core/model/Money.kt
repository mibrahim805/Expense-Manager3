package com.expensemanager.android.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun parseAmountToMinorUnits(raw: String): Long? = runCatching {
    val normalized = raw.replace(",", "").trim()
    if (normalized.isBlank()) return null
    BigDecimal(normalized)
        .setScale(2, RoundingMode.UNNECESSARY)
        .movePointRight(2)
        .longValueExact()
        .takeIf { it > 0L }
}.getOrNull()

fun formatAmountForInput(amountMinor: Long): String =
    BigDecimal.valueOf(amountMinor, 2).setScale(2).toPlainString()

fun formatMoney(amountMinor: Long, currencyCode: String = "PKR", withSign: Boolean = false): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "PK")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val amount = BigDecimal.valueOf(amountMinor, 2).abs()
    val prefix = if (currencyCode == "PKR") "Rs " else "$currencyCode "
    val sign = when {
        !withSign -> ""
        amountMinor > 0 -> "+"
        amountMinor < 0 -> "−"
        else -> ""
    }
    return "$sign$prefix${formatter.format(amount)}"
}

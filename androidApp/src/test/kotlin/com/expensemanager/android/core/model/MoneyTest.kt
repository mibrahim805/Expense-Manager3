package com.expensemanager.android.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun parsesTwoDecimalAmountsIntoMinorUnits() {
        assertEquals(245000L, parseAmountToMinorUnits("2,450.00"))
        assertEquals(99L, parseAmountToMinorUnits("0.99"))
    }

    @Test
    fun rejectsInvalidOrNonPositiveAmounts() {
        assertNull(parseAmountToMinorUnits(""))
        assertNull(parseAmountToMinorUnits("0"))
        assertNull(parseAmountToMinorUnits("1.999"))
        assertNull(parseAmountToMinorUnits("not a number"))
    }
}

package com.expensemanager.android.core.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {
    @Test
    fun newerSemanticVersionIsDetected() {
        assertTrue(isNewerVersion("1.2.0", "1.1.0"))
        assertTrue(isNewerVersion("v2.0", "1.9.9"))
        assertTrue(isNewerVersion("1.1.1", "1.1"))
    }

    @Test
    fun sameOrOlderVersionIsIgnored() {
        assertFalse(isNewerVersion("1.1.0", "1.1"))
        assertFalse(isNewerVersion("1.0.9", "1.1.0"))
        assertFalse(isNewerVersion("not-a-version", "1.1.0"))
    }
}

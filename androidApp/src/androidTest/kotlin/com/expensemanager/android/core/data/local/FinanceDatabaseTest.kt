package com.expensemanager.android.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensemanager.android.core.model.FinanceCategory
import com.expensemanager.android.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceDatabaseTest {
    private lateinit var context: Context
    private lateinit var databaseName: String
    private var database: FinanceDatabase? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        databaseName = "finance-test-${System.nanoTime()}.db"
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun transactionsPersistAfterDatabaseReopenAndDuplicateRequestIsIgnored() = runBlocking {
        database = openDatabase()
        val transaction = TransactionEntity(
            type = TransactionType.EXPENSE.name,
            amountMinor = 2_450,
            currencyCode = "PKR",
            categoryKey = FinanceCategory.FOOD.key,
            description = "Lunch",
            accountId = "cash",
            accountName = "Cash",
            timestampMillis = 1,
            requestId = "stable-request-id",
        )

        assertEquals(1L, database!!.transactionDao().insert(transaction))
        assertEquals(-1L, database!!.transactionDao().insert(transaction))
        database!!.close()

        database = openDatabase()
        val records = database!!.transactionDao().observeAll().first()

        assertEquals(1, records.size)
        assertEquals(2_450L, records.single().amountMinor)
        assertEquals("stable-request-id", records.single().requestId)
    }

    @Test
    fun budgetsPersistAfterDatabaseReopen() = runBlocking {
        database = openDatabase()
        database!!.budgetDao().upsert(
            BudgetEntity(
                categoryKey = FinanceCategory.FOOD.key,
                limitMinor = 25_000,
            )
        )
        database!!.close()

        database = openDatabase()
        val budgets = database!!.budgetDao().observeAll().first()

        assertEquals(1, budgets.size)
        assertEquals(FinanceCategory.FOOD.key, budgets.single().categoryKey)
        assertEquals(25_000L, budgets.single().limitMinor)
    }

    private fun openDatabase(): FinanceDatabase =
        Room.databaseBuilder(context, FinanceDatabase::class.java, databaseName)
            .addMigrations(*FinanceDatabase.migrations)
            .build()
}

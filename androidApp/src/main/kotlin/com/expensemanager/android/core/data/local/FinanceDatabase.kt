package com.expensemanager.android.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, BudgetEntity::class], version = 5, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val LOCAL_OWNER_ID = "local"
        @Volatile
        private var instance: FinanceDatabase? = null

        fun getInstance(context: Context): FinanceDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance.db",
                ).addMigrations(*migrations).build().also { instance = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN destinationAccountId TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN destinationAccountName TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN requestId TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_requestId ON transactions(requestId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS budgets (" +
                        "categoryKey TEXT NOT NULL, " +
                        "limitMinor INTEGER NOT NULL, " +
                        "currencyCode TEXT NOT NULL, " +
                        "PRIMARY KEY(categoryKey))"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN ownerId TEXT NOT NULL DEFAULT '$LOCAL_OWNER_ID'")
                db.execSQL(
                    "CREATE TABLE budgets_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "ownerId TEXT NOT NULL DEFAULT '$LOCAL_OWNER_ID', " +
                        "categoryKey TEXT NOT NULL, " +
                        "limitMinor INTEGER NOT NULL, " +
                        "currencyCode TEXT NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO budgets_new (ownerId, categoryKey, limitMinor, currencyCode) " +
                        "SELECT '$LOCAL_OWNER_ID', categoryKey, limitMinor, currencyCode FROM budgets"
                )
                db.execSQL("DROP TABLE budgets")
                db.execSQL("ALTER TABLE budgets_new RENAME TO budgets")
                db.execSQL("CREATE UNIQUE INDEX index_budgets_ownerId_categoryKey ON budgets(ownerId, categoryKey)")
            }
        }

        val migrations: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }
}

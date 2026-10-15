package com.expensemanager.android.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class], version = 3, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
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

        val migrations: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}

package com.expensemanager.android.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long): Int
}

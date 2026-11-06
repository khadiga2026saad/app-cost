package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entities.CurrentAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentAccountDao {

    @Query("SELECT * FROM current_account_treasury WHERE accountId = 1 LIMIT 1")
    fun getAccountFlow(): Flow<CurrentAccountEntity?>

    @Query("SELECT * FROM current_account_treasury WHERE accountId = 1 LIMIT 1")
    suspend fun getAccountDirect(): CurrentAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: CurrentAccountEntity)

    @Transaction
    suspend fun updateBalance(newBalancePiastres: Long, reconciledAt: Long) {
        val account = getAccountDirect() ?: CurrentAccountEntity()
        insertOrUpdate(
            account.copy(
                currentBalancePiastres = newBalancePiastres,
                lastReconciledAt = reconciledAt
            )
        )
    }
}

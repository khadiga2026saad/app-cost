package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.HouseholdExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdExpenseDao {
    @Query("SELECT * FROM household_expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<HouseholdExpenseEntity>>

    @Query("SELECT * FROM household_expenses WHERE date >= :start AND date < :end ORDER BY date DESC, id DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<HouseholdExpenseEntity>>

    @Query("SELECT * FROM household_expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HouseholdExpenseEntity?

    @Insert
    suspend fun insert(item: HouseholdExpenseEntity): Long

    @Update
    suspend fun update(item: HouseholdExpenseEntity)

    @Delete
    suspend fun delete(item: HouseholdExpenseEntity)

    @Query("DELETE FROM household_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}

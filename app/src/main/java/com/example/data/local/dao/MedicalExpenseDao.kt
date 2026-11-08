package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.MedicalExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalExpenseDao {
    @Query("SELECT * FROM medical_expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<MedicalExpenseEntity>>

    @Query("SELECT * FROM medical_expenses WHERE date >= :start AND date < :end ORDER BY date DESC, id DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<MedicalExpenseEntity>>

    @Query("SELECT * FROM medical_expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MedicalExpenseEntity?

    @Insert
    suspend fun insert(item: MedicalExpenseEntity): Long

    @Update
    suspend fun update(item: MedicalExpenseEntity)

    @Delete
    suspend fun delete(item: MedicalExpenseEntity)

    @Query("DELETE FROM medical_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, expenseId DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, expenseId DESC")
    fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE department = :department AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDepartmentBetween(department: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE studentId = :studentId ORDER BY date DESC")
    fun getExpensesByStudent(studentId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByStudentBetween(studentId: Long, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getExpensesByVehicle(vehicleId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseId = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE vehicleLogId = :vehicleLogId LIMIT 1")
    suspend fun getExpenseByVehicleLogId(vehicleLogId: Long): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE expenseId = :id")
    suspend fun deleteExpenseById(id: Long)
}

package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY fullName ASC")
    fun getActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)
}

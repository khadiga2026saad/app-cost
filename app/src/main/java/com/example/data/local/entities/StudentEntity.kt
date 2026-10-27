package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Student entity for education expenses mapping.
 * Adheres to Section 9.3 of the Financial Constitution.
 */
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["fullName"], unique = true)
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val studentId: Long = 0,
    val fullName: String,
    val academicStage: String, // ابتدائي، إعدادي، ثانوي، جامعي، إلخ
    val schoolOrInstitution: String? = null,
    val academicYear: String, // 2025/2026
    val monthlyBudgetPiastres: Long? = null,
    val isActive: Boolean = true
)

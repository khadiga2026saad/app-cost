package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "medical_expenses", indices = [Index("date"), Index("patientName")])
data class MedicalExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientName: String,
    val facilityType: String,
    val facilityName: String,
    val conditionDescription: String,
    val doctorDiagnosis: String = "",
    val checkupCostPiastres: Long = 0,
    val medicationCostPiastres: Long = 0,
    val date: Long,
    val notes: String = "",
    val attachmentsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalCostPiastres: Long get() = checkupCostPiastres + medicationCostPiastres
}

package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "household_expenses", indices = [Index(value = ["date"]), Index(value = ["category"])])
data class HouseholdExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amountPiastres: Long,
    val date: Long,
    val expenseType: String,
    val notes: String = "",
    val attachmentsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

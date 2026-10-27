package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Oil and filter change log adhering to Section 9.4.D of the Financial Constitution.
 */
@Entity(
    tableName = "vehicle_oil_logs",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["linkedExpenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["linkedExpenseId"], unique = true),
        Index(value = ["date"])
    ]
)
data class VehicleOilLogEntity(
    @PrimaryKey(autoGenerate = true)
    val oilLogId: Long = 0,
    val vehicleId: Long,
    val date: Long,
    val odometerAtChangeKm: Long,
    val oilBrandAndGrade: String,
    val intervalKm: Long,
    val nextDueOdometerKm: Long,
    val isFilterReplaced: Boolean,
    val totalCostPiastres: Long,
    val linkedExpenseId: Long
)

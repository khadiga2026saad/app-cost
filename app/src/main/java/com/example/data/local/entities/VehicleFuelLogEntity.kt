package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fuel log entity adhering to Section 9.4.B of the Financial Constitution.
 */
@Entity(
    tableName = "vehicle_fuel_logs",
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
data class VehicleFuelLogEntity(
    @PrimaryKey(autoGenerate = true)
    val fuelLogId: Long = 0,
    val vehicleId: Long,
    val date: Long,
    val odometerKm: Long,
    val quantityLiters: Double,
    val pricePerLiterPiastres: Long,
    val totalCostPiastres: Long,
    val isFullTank: Boolean,
    val stationName: String? = null,
    val fuelType: String = "بنزين 95",
    val linkedExpenseId: Long
)

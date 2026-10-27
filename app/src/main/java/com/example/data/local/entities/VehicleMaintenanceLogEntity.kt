package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Maintenance and spare parts log adhering to Section 9.4.C of the Financial Constitution.
 */
@Entity(
    tableName = "vehicle_maintenance_logs",
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
data class VehicleMaintenanceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val maintenanceLogId: Long = 0,
    val vehicleId: Long,
    val date: Long,
    val odometerKm: Long,
    val maintenanceType: String, // PERIODIC_SERVICE, UNSCHEDULED_REPAIR, INSPECTION_LICENSING
    val description: String,
    val partsCostPiastres: Long = 0L,
    val laborCostPiastres: Long = 0L,
    val additionalFeesPiastres: Long = 0L,
    val totalCostPiastres: Long,
    val serviceCenter: String? = null,
    val invoiceRef: String? = null,
    val linkedExpenseId: Long
)

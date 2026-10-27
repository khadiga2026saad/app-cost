package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vehicle accident log entity adhering to Section 9.4.E of the Financial Constitution.
 */
@Entity(
    tableName = "vehicle_accident_logs",
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
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["linkedExpenseId"], unique = true),
        Index(value = ["accidentDate"])
    ]
)
data class VehicleAccidentLogEntity(
    @PrimaryKey(autoGenerate = true)
    val accidentLogId: Long = 0,
    val vehicleId: Long,
    val accidentDate: Long,
    val odometerKm: Long? = null,
    val description: String,
    val estimatedDamagePiastres: Long = 0L,
    val actualRepairCostPiastres: Long = 0L,
    val insuranceCompensationPiastres: Long = 0L,
    val netUserCostPiastres: Long, // MAX(0, actualRepairCost - insuranceCompensation)
    val compensationStatus: String, // PENDING_CLAIM, COMPENSATED, SELF_COVERED, REJECTED
    val linkedExpenseId: Long? = null
)

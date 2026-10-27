package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vehicle entity adhering to Section 9.4.A of the Financial Constitution.
 */
@Entity(
    tableName = "vehicles",
    indices = [
        Index(value = ["plateNumber"], unique = true)
    ]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val vehicleId: Long = 0,
    val name: String,
    val plateNumber: String,
    val initialOdometerKm: Long,
    val currentOdometerKm: Long,
    val fuelTankCapacityLiters: Double? = null,
    val oilChangeIntervalKm: Long = 5000L,
    val lastOilChangeKm: Long = 0L,
    val isActive: Boolean = true
) {
    val id: Long get() = vehicleId
    val initialOdometer: Int get() = initialOdometerKm.toInt()
    val modelYear: String get() = ""
    val notes: String get() = ""
}

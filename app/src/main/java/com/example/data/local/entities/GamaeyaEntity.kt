package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Gamaeya (Money Circle) entity adhering to Section 9.5.A of the Financial Constitution.
 */
@Entity(tableName = "gamaeyat")
data class GamaeyaEntity(
    @PrimaryKey(autoGenerate = true)
    val gamaeyaId: Long = 0,
    val name: String,
    val totalPoolValuePiastres: Long,
    val numberOfMembers: Int,
    val monthlyInstallmentPiastres: Long,
    val myTurnOrder: Int,
    val startDate: Long,
    val expectedPayoutDate: Long,
    val actualPayoutDate: Long? = null,
    val isPayoutReceived: Boolean = false,
    val status: String = "ACTIVE" // ACTIVE, COMPLETED, CANCELLED
)

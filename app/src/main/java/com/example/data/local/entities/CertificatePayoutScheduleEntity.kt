package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Certificate payout schedule entity adhering to Section 9.6.C of the Financial Constitution.
 */
@Entity(
    tableName = "certificate_payout_schedules",
    foreignKeys = [
        ForeignKey(
            entity = FinancialCertificateEntity::class,
            parentColumns = ["certificateId"],
            childColumns = ["certificateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RevenueEntity::class,
            parentColumns = ["revenueId"],
            childColumns = ["linkedRevenueId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["certificateId"]),
        Index(value = ["payoutKey"], unique = true),
        Index(value = ["expectedDueDate", "isCollected"]),
        Index(value = ["linkedRevenueId"], unique = true)
    ]
)
data class CertificatePayoutScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val scheduleId: Long = 0,
    val certificateId: Long,
    val cycleNumber: Int,
    val expectedDueDate: Long,
    val expectedAmountPiastres: Long,
    val actualCollectedDate: Long? = null,
    val isCollected: Boolean = false,
    val payoutKey: String,
    val linkedRevenueId: Long? = null
)

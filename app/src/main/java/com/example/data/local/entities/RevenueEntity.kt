package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.MoneyUtils

/**
 * Revenue and earned income entity adhering to Section 9.6.A of the Financial Constitution.
 */
@Entity(
    tableName = "revenues",
    foreignKeys = [
        ForeignKey(
            entity = FinancialCertificateEntity::class,
            parentColumns = ["certificateId"],
            childColumns = ["certificateId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["sourceType"]),
        Index(value = ["certificateId"]),
        Index(value = ["cycleKey"], unique = true)
    ]
)
data class RevenueEntity(
    @PrimaryKey(autoGenerate = true)
    val revenueId: Long = 0,
    val amountPiastres: Long,
    val date: Long,
    val sourceType: String, // SALARY, BONUSES, ADDITIONAL_FREELANCE, OTHER_INCOME, CERTIFICATE_YIELD
    val title: String,
    val payerOrInstitution: String? = null,
    val isAccrualCollection: Boolean = false,
    val accrualReferenceId: Long? = null,
    val certificateId: Long? = null,
    val cycleKey: String? = null,
    val notes: String? = null
) {
    val id: Long get() = revenueId
    val amount: String get() = MoneyUtils.formatPiastresPlain(amountPiastres)
    val category: String get() = sourceType
    val vehicleId: Long? get() = null
}

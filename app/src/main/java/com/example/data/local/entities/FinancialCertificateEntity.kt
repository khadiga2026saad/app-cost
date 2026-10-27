package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.MoneyUtils

/**
 * Bank certificate and deposit entity adhering to Section 9.6.B of the Financial Constitution.
 */
@Entity(tableName = "financial_certificates")
data class FinancialCertificateEntity(
    @PrimaryKey(autoGenerate = true)
    val certificateId: Long = 0,
    val name: String,
    val bankName: String,
    val principalPiastres: Long,
    val annualRatePercent: String, // String representation of BigDecimal percentage (e.g. "23.50")
    val payoutFrequency: String, // MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY, AT_MATURITY
    val durationYears: Int,
    val issueDate: Long,
    val maturityDate: Long,
    val isActive: Boolean = true,
    val isRedeemedEarly: Boolean = false,
    val redemptionPenaltyPiastres: Long = 0L
) {
    val id: Long get() = certificateId
    val principalAmount: String get() = MoneyUtils.formatPiastresPlain(principalPiastres)
    val annualInterestRate: Double get() = annualRatePercent.toDoubleOrNull() ?: 0.0
    val notes: String get() = ""
}

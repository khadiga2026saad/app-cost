package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Non-operational treasury cash flow movement entity (Section 9.7 of Constitution).
 * Captures deposits, withdrawals, and balance reconciliations without distorting earned revenues or expenses.
 */
@Entity(
    tableName = "treasury_movements",
    indices = [
        Index(value = ["date"]),
        Index(value = ["movementType"])
    ]
)
data class TreasuryMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val movementId: Long = 0,
    val movementType: String, // DEPOSIT, WITHDRAWAL, BALANCE_ADJUSTMENT
    val amountPiastres: Long,
    val date: Long,
    val reason: String,
    val balanceBeforePiastres: Long,
    val balanceAfterPiastres: Long
)

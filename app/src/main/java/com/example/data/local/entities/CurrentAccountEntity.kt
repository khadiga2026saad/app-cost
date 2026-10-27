package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single Liquid Treasury Account entity representing the user's primary cash pool.
 * Strictly adheres to Section 9.1 of the Financial Constitution.
 */
@Entity(tableName = "current_account_treasury")
data class CurrentAccountEntity(
    @PrimaryKey
    val accountId: Long = 1L,
    val accountName: String = "الخزينة الرئيسية",
    val currentBalancePiastres: Long = 0L,
    val openingBalancePiastres: Long = 0L,
    val lastReconciledAt: Long = System.currentTimeMillis()
)

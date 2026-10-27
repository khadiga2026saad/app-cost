package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Gamaeya installment entity adhering to Section 9.5.B of the Financial Constitution.
 */
@Entity(
    tableName = "gamaeya_installments",
    foreignKeys = [
        ForeignKey(
            entity = GamaeyaEntity::class,
            parentColumns = ["gamaeyaId"],
            childColumns = ["gamaeyaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["gamaeyaId", "installmentNumber"], unique = true),
        Index(value = ["idempotencyKey"], unique = true),
        Index(value = ["dueDate", "isPaid"])
    ]
)
data class GamaeyaInstallmentEntity(
    @PrimaryKey(autoGenerate = true)
    val installmentId: Long = 0,
    val gamaeyaId: Long,
    val installmentNumber: Int,
    val dueDate: Long,
    val paidDate: Long? = null,
    val amountPiastres: Long,
    val isPaid: Boolean = false,
    val idempotencyKey: String,
    val notes: String? = null
)

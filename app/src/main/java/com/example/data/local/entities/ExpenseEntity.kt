package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.MoneyUtils

/**
 * Main and specialized expense entity with foreign key relationships.
 * Adheres strictly to Section 9.2 of the Financial Constitution.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["vehicleId"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["department"]),
        Index(value = ["studentId"]),
        Index(value = ["vehicleId"]),
        Index(value = ["vehicleLogId"], unique = true)
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val amountPiastres: Long,
    val date: Long,
    val department: String, // HOME, MEDICAL, PERSONAL, WORK, EDUCATION, VEHICLES
    val subcategory: String,
    val title: String,
    val notes: String? = null,
    val vendorOrPayer: String? = null,
    val referenceInvoice: String? = null,
    val studentId: Long? = null,
    val vehicleId: Long? = null,
    val vehicleLogId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val id: Long get() = expenseId
    val amount: String get() = MoneyUtils.formatPiastresPlain(amountPiastres)
    val category: String get() = subcategory
    val studentName: String? get() = null
    val projectName: String? get() = null
    val institution: String? get() = null
    val academicTerm: String? get() = null
    val isTaxDeductible: Boolean get() = false
}

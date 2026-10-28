package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Account key is the Firebase UID, or "guest" for the local guest workspace. */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val accountKey: String,
    val fullName: String = "",
    val nickname: String = "",
    /** ISO-8601 yyyy-MM-dd; null when absent or invalid. */
    val birthDate: String? = null,
    val phone: String? = null,
    val photoPath: String? = null,
    val currency: String = "EGP",
    val dateFormat: String = "dd/MM/yyyy",
    val numberLanguage: String = "latn"
)

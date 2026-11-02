package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.room.withTransaction
import com.example.data.local.entities.UserProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * Call for each authenticated UID (or guest) before switching profile readers to Room.
 * Old preferences are retained until the Room transaction commits.
 */
class ProfilePreferencesMigrator(private val database: AppDatabase, private val context: Context) {
    suspend fun migrate(accountKey: String, fallbackName: String = "") {
        require(accountKey.isNotBlank())
        val legacy = context.getSharedPreferences("profile_" + accountKey, Context.MODE_PRIVATE)
        val marker = context.getSharedPreferences("profile_room_migration", Context.MODE_PRIVATE)
        val markerKey = "migrated_" + accountKey
        if (marker.getBoolean(markerKey, false)) return
        database.withTransaction {
            if (database.profileDao().get(accountKey) == null) {
                database.profileDao().upsert(
                    UserProfile(
                        accountKey = accountKey,
                        fullName = legacy.getString("full_name", fallbackName).orEmpty(),
                        nickname = legacy.getString("nickname", "").orEmpty(),
                        birthDate = normalizeBirthDate(legacy.getString("birth_date", null)),
                        phone = legacy.getString("phone", null),
                        photoPath = legacy.getString("photo_path", null),
                        currency = legacy.getString("currency", "EGP") ?: "EGP",
                        dateFormat = legacy.getString("date_format", "dd/MM/yyyy") ?: "dd/MM/yyyy",
                        numberLanguage = legacy.getString("number_language", "latn") ?: "latn"
                    )
                )
            }
        }
        // Never mark as migrated before the successful transaction.
        check(marker.edit().putBoolean(markerKey, true).commit()) { "Could not persist profile migration marker" }
        // Preserve legacy values as a recovery backup; do not clear them.
    }

    companion object {
        fun normalizeBirthDate(raw: String?): String? {
            val value = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val normalizedDigits = value.map { char ->
                when (char) {
                    in '\u0660'..'\u0669' -> ('0'.code + (char.code - '\u0660'.code)).toChar()
                    in '\u06F0'..'\u06F9' -> ('0'.code + (char.code - '\u06F0'.code)).toChar()
                    else -> char
                }
            }.joinToString("")
            val formats = listOf(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/uuuu"),
                DateTimeFormatter.ofPattern("d/M/uuuu"),
                DateTimeFormatter.ofPattern("dd-MM-uuuu"),
                DateTimeFormatter.ofPattern("d-M-uuuu")
            ).map { it.withResolverStyle(ResolverStyle.STRICT) }
            return formats.firstNotNullOfOrNull { format ->
                runCatching { LocalDate.parse(normalizedDigits, format).format(DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
            }
        }
    }
}

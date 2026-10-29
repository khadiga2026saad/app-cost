package com.example.data.repository

import com.example.data.local.ProfilePreferencesMigrator
import com.example.data.local.dao.ProfileDao
import com.example.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

/** A profile is always scoped to a single account; no global profile fallback. */
class ProfileRepository(
    private val dao: ProfileDao,
    private val migrator: ProfilePreferencesMigrator
) {
    fun observe(accountKey: String): Flow<UserProfile?> {
        require(accountKey.isNotBlank())
        return dao.observe(accountKey)
    }

    suspend fun save(profile: UserProfile) {
        require(profile.accountKey.isNotBlank())
        dao.upsert(profile)
    }

    /** Safe to retry: the migrator checks both its committed marker and the existing Room row. */
    suspend fun ensureMigrated(accountKey: String, fallbackName: String = "") {
        require(accountKey.isNotBlank())
        migrator.migrate(accountKey, fallbackName)
    }
}

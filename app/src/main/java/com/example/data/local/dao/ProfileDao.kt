package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profiles WHERE accountKey = :accountKey LIMIT 1")
    fun observe(accountKey: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE accountKey = :accountKey LIMIT 1")
    suspend fun get(accountKey: String): UserProfile?

    @Upsert
    suspend fun upsert(profile: UserProfile)
}

package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entities.UserProfile
import com.example.data.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProfileRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: ProfileRepository
    private val keys = listOf("profile_test_existing", "profile_test_empty", "profile_test_twice")

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        keys.forEach { key ->
            context.getSharedPreferences("profile_" + key, Context.MODE_PRIVATE).edit().clear().commit()
            context.getSharedPreferences("profile_room_migration", Context.MODE_PRIVATE).edit()
                .remove("migrated_" + key).commit()
        }
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        repository = ProfileRepository(database.profileDao(), ProfilePreferencesMigrator(database, context))
    }

    @After fun tearDown() {
        database.close()
        keys.forEach { key ->
            context.getSharedPreferences("profile_" + key, Context.MODE_PRIVATE).edit().clear().commit()
            context.getSharedPreferences("profile_room_migration", Context.MODE_PRIVATE).edit()
                .remove("migrated_" + key).commit()
        }
    }

    @Test fun existingLegacyValuesAreCopiedAndPreserved() = runBlocking {
        val key = keys[0]
        val legacy = context.getSharedPreferences("profile_" + key, Context.MODE_PRIVATE)
        legacy.edit().putString("full_name", "Existing User").putString("nickname", "Nick")
            .putString("birth_date", "29/02/2000").putString("phone", "123")
            .putString("currency", "USD").commit()
        repository.ensureMigrated(key)
        val profile = repository.observe(key).first()
        assertEquals("Existing User", profile?.fullName)
        assertEquals("Nick", profile?.nickname)
        assertEquals("2000-02-29", profile?.birthDate)
        assertEquals("123", profile?.phone)
        assertEquals("USD", profile?.currency)
        assertEquals("Existing User", legacy.getString("full_name", null))
    }

    @Test fun emptyLegacyCreatesAnEmptyProfileWithoutCrossAccountFallback() = runBlocking {
        val key = keys[1]
        repository.ensureMigrated(key)
        assertEquals(UserProfile(accountKey = key), repository.observe(key).first())
        assertNull(repository.observe(keys[0]).first())
    }

    @Test fun secondMigrationDoesNotOverwriteSavedRoomProfile() = runBlocking {
        val key = keys[2]
        val legacy = context.getSharedPreferences("profile_" + key, Context.MODE_PRIVATE)
        legacy.edit().putString("full_name", "Original").commit()
        repository.ensureMigrated(key)
        repository.save(UserProfile(accountKey = key, fullName = "Edited in Room"))
        legacy.edit().putString("full_name", "Changed legacy").commit()
        repository.ensureMigrated(key)
        assertEquals("Edited in Room", repository.observe(key).first()?.fullName)
        assertEquals("Changed legacy", legacy.getString("full_name", null))
    }
}

package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileMigrationInstrumentedTest {
    @Test fun migrationOnlyAddsProfileAndPreservesFinancialRows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "profile_migration_test.db"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE expenses (id INTEGER PRIMARY KEY, amount INTEGER)")
                        db.execSQL("CREATE TABLE revenues (id INTEGER PRIMARY KEY, amount INTEGER)")
                        db.execSQL("INSERT INTO expenses VALUES (1, 100)")
                        db.execSQL("INSERT INTO revenues VALUES (1, 200)")
                    }
                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                }).build()
        )
        val db = helper.writableDatabase
        fun count(table: String): Long = db.query("SELECT COUNT(*) FROM $table").use { it.moveToFirst(); it.getLong(0) }
        val beforeExpenses = count("expenses")
        val beforeRevenues = count("revenues")
        AppDatabase.MIGRATION_2_3.migrate(db)
        assertEquals(beforeExpenses, count("expenses"))
        assertEquals(beforeRevenues, count("revenues"))
        assertEquals(0L, count("user_profiles"))
        helper.close()
        context.deleteDatabase(name)
    }

    @Test fun legacyProfileIsCopiedOnlyOnce() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "migration_test_uid"
        context.getSharedPreferences("profile_" + key, Context.MODE_PRIVATE).edit().clear()
            .putString("full_name", "Test User").putString("birth_date", "29/02/2000").commit()
        context.getSharedPreferences("profile_room_migration", Context.MODE_PRIVATE).edit()
            .remove("migrated_" + key).commit()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val migrator = ProfilePreferencesMigrator(db, context)
            migrator.migrate(key)
            assertEquals("2000-02-29", db.profileDao().get(key)?.birthDate)
            context.getSharedPreferences("profile_" + key, Context.MODE_PRIVATE).edit()
                .putString("full_name", "Changed legacy name").commit()
            migrator.migrate(key)
            assertEquals("Test User", db.profileDao().get(key)?.fullName)
        } finally {
            db.close()
            context.getSharedPreferences("profile_room_migration", Context.MODE_PRIVATE).edit()
                .remove("migrated_" + key).commit()
        }
    }
}

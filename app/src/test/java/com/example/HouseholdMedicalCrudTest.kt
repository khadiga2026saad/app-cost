package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.HouseholdExpenseEntity
import com.example.data.local.entities.MedicalExpenseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HouseholdMedicalCrudTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun householdExpense_supportsInsertObserveUpdateDelete() = runBlocking {
        val dao = db.householdExpenseDao()
        val id = dao.insert(HouseholdExpenseEntity(category = "طعام", amountPiastres = 12550L, date = 1_000L, expenseType = "PURCHASES_AND_SUPPLIES"))
        assertTrue(id > 0)
        assertEquals(1, dao.observeAll().first().size)
        val saved = dao.getById(id)!!
        dao.update(saved.copy(amountPiastres = 15000L, notes = "تعديل"))
        assertEquals(15000L, dao.getById(id)?.amountPiastres)
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun medicalExpense_supportsInsertObserveUpdateDeleteAndSeparateCosts() = runBlocking {
        val dao = db.medicalExpenseDao()
        val id = dao.insert(MedicalExpenseEntity(
            patientName = "اسم يدوي", facilityType = "CLINIC", facilityName = "عيادة",
            conditionDescription = "كشف عام", doctorDiagnosis = "تشخيص",
            checkupCostPiastres = 20000L, medicationCostPiastres = 7500L, date = 2_000L
        ))
        assertTrue(id > 0)
        assertEquals(27500L, dao.getById(id)?.totalCostPiastres)
        val saved = dao.getById(id)!!
        dao.update(saved.copy(patientName = "اسم آخر", medicationCostPiastres = 9000L))
        assertEquals("اسم آخر", dao.getById(id)?.patientName)
        assertEquals(29000L, dao.getById(id)?.totalCostPiastres)
        assertEquals(1, dao.observeAll().first().size)
        dao.delete(dao.getById(id)!!)
        assertNull(dao.getById(id))
    }
}

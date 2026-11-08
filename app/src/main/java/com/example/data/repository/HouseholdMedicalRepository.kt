package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.entities.HouseholdExpenseEntity
import com.example.data.local.entities.MedicalExpenseEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.io.File
import java.util.UUID

class HouseholdMedicalRepository(context: Context, private val db: AppDatabase) {
    private val appContext = context.applicationContext
    private val householdDao = db.householdExpenseDao()
    private val medicalDao = db.medicalExpenseDao()

    fun observeHousehold(): Flow<List<HouseholdExpenseEntity>> = householdDao.observeAll()
    fun observeMedical(): Flow<List<MedicalExpenseEntity>> = medicalDao.observeAll()

    suspend fun saveHousehold(item: HouseholdExpenseEntity, attachmentUris: List<Uri>): Long {
        val paths = persistAttachments(attachmentUris, "household")
        return householdDao.insert(item.copy(attachmentsJson = JSONArray(paths).toString()))
    }

    suspend fun updateHousehold(item: HouseholdExpenseEntity, attachmentUris: List<Uri>): Long {
        val newPaths = persistAttachments(attachmentUris, "household")
        val oldPaths = JSONArray(item.attachmentsJson)
        val combined = JSONArray()
        for (i in 0 until oldPaths.length()) combined.put(oldPaths.getString(i))
        newPaths.forEach { combined.put(it) }
        householdDao.update(item.copy(attachmentsJson = combined.toString(), updatedAt = System.currentTimeMillis()))
        return item.id
    }

    suspend fun saveMedical(item: MedicalExpenseEntity, attachmentUris: List<Uri>): Long {
        val paths = persistAttachments(attachmentUris, "medical")
        return medicalDao.insert(item.copy(attachmentsJson = JSONArray(paths).toString()))
    }

    suspend fun updateMedical(item: MedicalExpenseEntity, attachmentUris: List<Uri>): Long {
        val newPaths = persistAttachments(attachmentUris, "medical")
        val oldPaths = JSONArray(item.attachmentsJson)
        val combined = JSONArray()
        for (i in 0 until oldPaths.length()) combined.put(oldPaths.getString(i))
        newPaths.forEach(combined::put)
        medicalDao.update(item.copy(attachmentsJson = combined.toString(), updatedAt = System.currentTimeMillis()))
        return item.id
    }

    suspend fun deleteHousehold(id: Long) {
        householdDao.getById(id)?.let { deleteStoredAttachments(it.attachmentsJson); householdDao.delete(it) }
    }

    suspend fun deleteMedical(id: Long) {
        medicalDao.getById(id)?.let { deleteStoredAttachments(it.attachmentsJson); medicalDao.delete(it) }
    }

    private fun persistAttachments(uris: List<Uri>, folder: String): List<String> {
        if (uris.isEmpty()) return emptyList()
        val destination = File(appContext.filesDir, "financial_attachments/$folder").apply { mkdirs() }
        return uris.mapNotNull { uri ->
            runCatching {
                val mime = appContext.contentResolver.getType(uri).orEmpty()
                val extension = when {
                    mime.contains("pdf") -> ".pdf"
                    mime.contains("png") -> ".png"
                    mime.contains("webp") -> ".webp"
                    else -> ".jpg"
                }
                val target = File(destination, "${UUID.randomUUID()}$extension")
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use(input::copyTo)
                } ?: error("Unable to open attachment")
                target.absolutePath
            }.getOrNull()
        }
    }

    private fun deleteStoredAttachments(json: String) {
        runCatching {
            val array = JSONArray(json)
            for (i in 0 until array.length()) File(array.getString(i)).delete()
        }
    }
}

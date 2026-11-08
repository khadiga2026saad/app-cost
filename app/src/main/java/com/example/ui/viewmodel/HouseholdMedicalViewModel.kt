package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.local.AppDatabase
import com.example.data.local.entities.HouseholdExpenseEntity
import com.example.data.local.entities.MedicalExpenseEntity
import com.example.data.repository.HouseholdMedicalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HouseholdMedicalViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HouseholdMedicalRepository(application, AppDatabase.getDatabase(application))

    val householdRecords: StateFlow<List<HouseholdExpenseEntity>> = repository.observeHousehold()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val medicalRecords: StateFlow<List<MedicalExpenseEntity>> = repository.observeMedical()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveHousehold(item: HouseholdExpenseEntity, attachments: List<Uri>, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch { onResult(runCatching { repository.saveHousehold(item, attachments) }) }
    }
    fun updateHousehold(item: HouseholdExpenseEntity, attachments: List<Uri>, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch { onResult(runCatching { repository.updateHousehold(item, attachments) }) }
    }
    fun deleteHousehold(id: Long, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch { onResult(runCatching { repository.deleteHousehold(id) }) }
    }
    fun saveMedical(item: MedicalExpenseEntity, attachments: List<Uri>, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch { onResult(runCatching { repository.saveMedical(item, attachments) }) }
    }
    fun updateMedical(item: MedicalExpenseEntity, attachments: List<Uri>, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch { onResult(runCatching { repository.updateMedical(item, attachments) }) }
    }
    fun deleteMedical(id: Long, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch { onResult(runCatching { repository.deleteMedical(id) }) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return HouseholdMedicalViewModel(app as Application) as T
            }
        }
    }
}

package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.data.local.entities.UserProfile
import com.example.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Construct a new instance for each account; never reuse one across account switches. */
class ProfileViewModel(
    private val accountKey: String,
    private val repository: ProfileRepository,
    fallbackName: String = ""
) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    init {
        require(accountKey.isNotBlank())
        viewModelScope.launch {
            try {
                repository.ensureMigrated(accountKey, fallbackName)
                _loading.value = false
                repository.observe(accountKey).collect { _profile.value = it }
            } catch (failure: Exception) {
                _error.value = failure
                _loading.value = false
            }
        }
    }

    fun save(profile: UserProfile) {
        require(profile.accountKey == accountKey) { "Profile belongs to another account" }
        viewModelScope.launch {
            try {
                repository.save(profile)
                _error.value = null
            } catch (failure: Exception) {
                _error.value = failure
            }
        }
    }

    class Factory(
        private val container: AppContainer,
        private val accountKey: String,
        private val fallbackName: String = ""
    ) : ViewModelProvider.Factory {
        init { require(accountKey.isNotBlank()) }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
            return ProfileViewModel(accountKey, container.profileRepository, fallbackName) as T
        }
    }
}

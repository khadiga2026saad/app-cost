package com.example

import android.app.Application
import android.util.Log
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartCostApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Firebase may restore the signed-in user after Application.onCreate.
        // Observe auth state here so each account's legacy profile is migrated before
        // profile screens consume it. The migrator is idempotent and preserves legacy prefs.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            val accountKey = user?.uid ?: "guest"
            val fallbackName = user?.displayName.orEmpty()
            applicationScope.launch {
                try {
                    container.profileRepository.ensureMigrated(accountKey, fallbackName)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    Log.e("SmartCostApplication", "Profile migration failed for account $accountKey", failure)
                }
            }
        }
    }
}

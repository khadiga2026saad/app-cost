package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartCostApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        // Start the idempotent legacy-profile migration as early as possible.
        val startupAccountKey = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { container.profileRepository.ensureMigrated(startupAccountKey) }
        }
    }
}

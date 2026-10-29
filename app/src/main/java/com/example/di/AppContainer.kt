package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ProfilePreferencesMigrator
import com.example.data.repository.FinanceRepository
import com.example.data.repository.FinanceRepositoryImpl
import com.example.data.repository.ProfileRepository
import com.example.domain.usecase.FinancialCalculationUseCase

interface AppContainer {
    val financeRepository: FinanceRepository
    val financialCalculationUseCase: FinancialCalculationUseCase
    val profileRepository: ProfileRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val financeRepository: FinanceRepository by lazy {
        FinanceRepositoryImpl(database)
    }

    override val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.profileDao(), ProfilePreferencesMigrator(database, context.applicationContext))
    }

    override val financialCalculationUseCase: FinancialCalculationUseCase by lazy {
        FinancialCalculationUseCase()
    }
}

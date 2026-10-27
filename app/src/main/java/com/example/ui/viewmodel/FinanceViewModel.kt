package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.SmartCostApplication
import com.example.core.DateUtils
import com.example.core.MoneyUtils
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.FinancialCertificateEntity
import com.example.data.local.entities.GamaeyaEntity
import com.example.data.local.entities.RevenueEntity
import com.example.data.local.entities.StudentEntity
import com.example.data.local.entities.VehicleEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.local.entities.VehicleMaintenanceLogEntity
import com.example.data.repository.FinanceRepository
import com.example.domain.model.FinancialSummary
import com.example.domain.model.MonthlyReportItem
import com.example.domain.model.VehicleSummary
import com.example.domain.usecase.FinancialCalculationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class MonthFilter(val year: Int, val month: Int)

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val calculationUseCase: FinancialCalculationUseCase
) : ViewModel() {

    private val _currentMonthFilter = MutableStateFlow(
        MonthFilter(DateUtils.currentYear(), DateUtils.currentMonth())
    )
    val currentMonthFilter: StateFlow<MonthFilter> = _currentMonthFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDeptFilter = MutableStateFlow("ALL")
    val selectedDeptFilter: StateFlow<String> = _selectedDeptFilter.asStateFlow()

    private val _sortByAmount = MutableStateFlow(false)
    val sortByAmount: StateFlow<Boolean> = _sortByAmount.asStateFlow()

    // Monthly data based on currentMonthFilter
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthSummary: StateFlow<FinancialSummary> = _currentMonthFilter.flatMapLatest { filter ->
        val start = DateUtils.getMonthStartEpoch(filter.year, filter.month)
        val end = DateUtils.getMonthEndEpoch(filter.year, filter.month)

        combine(
            repository.getAccountFlow(),
            repository.getExpensesBetween(start, end),
            repository.getRevenuesBetween(start, end),
            repository.getFuelLogsBetween(start, end),
            repository.getMaintenanceLogsBetween(start, end)
        ) { account, expenses, revenues, fuelLogs, maintLogs ->
            val balance = account?.currentBalancePiastres ?: 0L
            calculationUseCase.calculateSummary(
                currentBalancePiastres = balance,
                expenses = expenses,
                revenues = revenues,
                fuelLogs = fuelLogs,
                maintenanceLogs = maintLogs
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSummary()
    )

    // All expenses for current month with search and department filtering applied
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        _currentMonthFilter.flatMapLatest { filter ->
            val start = DateUtils.getMonthStartEpoch(filter.year, filter.month)
            val end = DateUtils.getMonthEndEpoch(filter.year, filter.month)
            repository.getExpensesBetween(start, end)
        },
        _searchQuery,
        _selectedDeptFilter,
        _sortByAmount
    ) { expenses, query, dept, sortAmount ->
        var result = expenses

        if (dept != "ALL") {
            result = result.filter { it.department.equals(dept, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                    it.subcategory.lowercase().contains(q) ||
                    (it.notes?.lowercase()?.contains(q) == true)
            }
        }

        if (sortAmount) {
            result.sortedByDescending { it.amountPiastres }
        } else {
            result.sortedByDescending { it.date }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All revenues for current month
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMonthRevenues: StateFlow<List<RevenueEntity>> = _currentMonthFilter.flatMapLatest { filter ->
        val start = DateUtils.getMonthStartEpoch(filter.year, filter.month)
        val end = DateUtils.getMonthEndEpoch(filter.year, filter.month)
        repository.getRevenuesBetween(start, end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Vehicles
    val allVehicles: StateFlow<List<VehicleEntity>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allVehicleLogs: StateFlow<List<com.example.domain.model.VehicleLogItem>> = combine(
        repository.getFuelLogsBetween(0L, Long.MAX_VALUE),
        repository.getMaintenanceLogsBetween(0L, Long.MAX_VALUE)
    ) { fuelLogs, maintLogs ->
        val fuelItems = fuelLogs.map {
            com.example.domain.model.VehicleLogItem(
                id = it.fuelLogId,
                vehicleId = it.vehicleId,
                logType = "FUEL",
                amount = MoneyUtils.formatPiastresPlain(it.totalCostPiastres),
                date = it.date,
                odometer = it.odometerKm.toInt(),
                liters = it.quantityLiters,
                notes = it.stationName ?: ""
            )
        }
        val maintItems = maintLogs.map {
            com.example.domain.model.VehicleLogItem(
                id = it.maintenanceLogId,
                vehicleId = it.vehicleId,
                logType = "MAINTENANCE",
                amount = MoneyUtils.formatPiastresPlain(it.totalCostPiastres),
                date = it.date,
                odometer = it.odometerKm.toInt(),
                serviceType = it.maintenanceType,
                serviceCenter = it.serviceCenter,
                notes = it.description
            )
        }
        (fuelItems + maintItems).sortedByDescending { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Vehicle Summaries per vehicle
    val vehicleSummaries: StateFlow<List<VehicleSummary>> = combine(
        repository.getAllVehicles(),
        repository.getAllExpenses()
    ) { vehicles, expenses ->
        vehicles.map { vehicle ->
            val vExpenses = expenses.filter { it.vehicleId == vehicle.vehicleId }
            calculationUseCase.calculateVehicleSummary(
                vehicle = vehicle,
                expenses = vExpenses,
                fuelLogs = emptyList(),
                maintenanceLogs = emptyList()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Financial Certificates
    val allCertificates: StateFlow<List<FinancialCertificateEntity>> = repository.getAllCertificates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Gamaeyat
    val allGamaeyat: StateFlow<List<GamaeyaEntity>> = repository.getAllGamaeyat()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Students
    val allStudents: StateFlow<List<StudentEntity>> = repository.getAllStudents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reports: Month by month comparison
    val monthlyReports: StateFlow<List<MonthlyReportItem>> = combine(
        repository.getAllExpenses(),
        repository.getAllRevenues()
    ) { expenses, revenues ->
        val currentYr = DateUtils.currentYear()
        (1..12).map { month ->
            val start = DateUtils.getMonthStartEpoch(currentYr, month)
            val end = DateUtils.getMonthEndEpoch(currentYr, month)
            val monthExpenses = expenses.filter { it.date in start..end }.sumOf { it.amountPiastres }
            val monthRevenues = revenues.filter { it.date in start..end && !it.isAccrualCollection }.sumOf { it.amountPiastres }
            MonthlyReportItem(
                year = currentYr,
                month = month,
                monthName = DateUtils.arabicMonthNames[month - 1],
                totalExpensesPiastres = monthExpenses,
                totalRevenuesPiastres = monthRevenues,
                netBalancePiastres = monthRevenues - monthExpenses
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setMonthFilter(year: Int, month: Int) {
        _currentMonthFilter.value = MonthFilter(year, month)
    }

    fun nextMonth() {
        val current = _currentMonthFilter.value
        val newMonth = if (current.month == 12) 1 else current.month + 1
        val newYear = if (current.month == 12) current.year + 1 else current.year
        _currentMonthFilter.value = MonthFilter(newYear, newMonth)
    }

    fun previousMonth() {
        val current = _currentMonthFilter.value
        val newMonth = if (current.month == 1) 12 else current.month - 1
        val newYear = if (current.month == 1) current.year - 1 else current.year
        _currentMonthFilter.value = MonthFilter(newYear, newMonth)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDepartmentFilter(deptId: String) {
        _selectedDeptFilter.value = deptId
    }

    fun toggleSortOrder() {
        _sortByAmount.value = !_sortByAmount.value
    }

    // CRUD Expense
    fun saveExpense(
        id: Long = 0,
        amount: BigDecimal,
        date: Long,
        department: String,
        category: String,
        title: String,
        notes: String = "",
        vehicleId: Long? = null,
        studentName: String? = null,
        institution: String? = null,
        academicTerm: String? = null,
        projectName: String? = null,
        isTaxDeductible: Boolean = false
    ) {
        viewModelScope.launch {
            val amountPiastres = MoneyUtils.egpToPiastres(amount)
            val entity = ExpenseEntity(
                expenseId = id,
                amountPiastres = amountPiastres,
                date = date,
                department = department,
                subcategory = category,
                title = title,
                notes = notes.ifBlank { null },
                vehicleId = vehicleId
            )
            if (id == 0L) {
                repository.insertExpense(entity)
            } else {
                val existing = repository.getExpenseById(id)
                if (existing != null) {
                    repository.updateExpense(existing, entity)
                } else {
                    repository.insertExpense(entity)
                }
            }
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    // CRUD Revenue
    fun saveRevenue(
        id: Long = 0,
        amount: BigDecimal,
        date: Long,
        category: String,
        title: String,
        notes: String = "",
        vehicleId: Long? = null,
        certificateId: Long? = null
    ) {
        viewModelScope.launch {
            val amountPiastres = MoneyUtils.egpToPiastres(amount)
            val entity = RevenueEntity(
                revenueId = id,
                amountPiastres = amountPiastres,
                date = date,
                sourceType = category,
                title = title,
                notes = notes.ifBlank { null },
                certificateId = certificateId
            )
            if (id == 0L) {
                repository.insertRevenue(entity)
            } else {
                val existing = repository.getRevenueById(id)
                if (existing != null) {
                    repository.updateRevenue(existing, entity)
                } else {
                    repository.insertRevenue(entity)
                }
            }
        }
    }

    fun deleteRevenue(id: Long) {
        viewModelScope.launch {
            repository.deleteRevenueById(id)
        }
    }

    // CRUD Vehicle
    fun saveVehicle(
        id: Long = 0,
        name: String,
        plateNumber: String,
        modelYear: String,
        initialOdometer: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entity = VehicleEntity(
                vehicleId = id,
                name = name,
                plateNumber = plateNumber,
                initialOdometerKm = initialOdometer.toLong(),
                currentOdometerKm = initialOdometer.toLong()
            )
            if (id == 0L) {
                repository.insertVehicle(entity)
            } else {
                repository.updateVehicle(entity)
            }
        }
    }

    fun deleteVehicle(id: Long) {
        viewModelScope.launch {
            val v = repository.getVehicleById(id)
            if (v != null) {
                repository.deleteVehicle(v)
            }
        }
    }

    // Vehicle Logs
    fun saveVehicleLog(
        id: Long = 0,
        vehicleId: Long,
        logType: String,
        amount: BigDecimal,
        date: Long,
        odometer: Int?,
        liters: Double?,
        serviceType: String?,
        serviceCenter: String?,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val vehicle = repository.getVehicleById(vehicleId)
            val vehicleName = vehicle?.name ?: "المركبة"
            val totalCostPiastres = MoneyUtils.egpToPiastres(amount)

            if (logType.equals("FUEL", ignoreCase = true)) {
                val fuelLog = VehicleFuelLogEntity(
                    fuelLogId = id,
                    vehicleId = vehicleId,
                    date = date,
                    odometerKm = odometer?.toLong() ?: 0L,
                    quantityLiters = liters ?: 0.0,
                    pricePerLiterPiastres = if ((liters ?: 0.0) > 0.0) {
                        (totalCostPiastres / (liters ?: 1.0)).toLong()
                    } else 0L,
                    totalCostPiastres = totalCostPiastres,
                    isFullTank = true,
                    linkedExpenseId = 0L
                )
                repository.insertFuelLogWithExpense(fuelLog, vehicleName)
            } else {
                val maintLog = VehicleMaintenanceLogEntity(
                    maintenanceLogId = id,
                    vehicleId = vehicleId,
                    date = date,
                    odometerKm = odometer?.toLong() ?: 0L,
                    maintenanceType = serviceType ?: "PERIODIC_SERVICE",
                    description = notes.ifBlank { "صيانة دورية" },
                    totalCostPiastres = totalCostPiastres,
                    serviceCenter = serviceCenter,
                    linkedExpenseId = 0L
                )
                repository.insertMaintenanceLogWithExpense(maintLog, vehicleName)
            }
        }
    }

    fun deleteVehicleLog(id: Long, logType: String = "") {
        viewModelScope.launch {
            repository.deleteVehicleLogById(id, logType)
        }
    }

    fun addExpense(
        amountEgp: String,
        department: String,
        subcategory: String,
        title: String,
        notes: String = ""
    ) {
        val amount = MoneyUtils.parseBigDecimal(amountEgp) ?: BigDecimal.ZERO
        if (amount > BigDecimal.ZERO) {
            saveExpense(
                amount = amount,
                date = DateUtils.currentEpochMillis(),
                department = department,
                category = subcategory,
                title = title,
                notes = notes
            )
        }
    }

    // Certificates
    fun saveCertificate(
        id: Long = 0,
        name: String,
        bankName: String,
        principalAmount: BigDecimal,
        annualInterestRate: Double,
        payoutFrequency: String,
        issueDate: Long,
        maturityDate: Long,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val principalPiastres = MoneyUtils.egpToPiastres(principalAmount)
            val entity = FinancialCertificateEntity(
                certificateId = id,
                name = name,
                bankName = bankName,
                principalPiastres = principalPiastres,
                annualRatePercent = annualInterestRate.toString(),
                payoutFrequency = payoutFrequency,
                durationYears = 3,
                issueDate = issueDate,
                maturityDate = maturityDate
            )
            repository.createCertificateWithSchedules(entity)
        }
    }

    fun deleteCertificate(id: Long) {
        viewModelScope.launch {
            val cert = repository.getCertificateById(id)
            if (cert != null) {
                repository.deleteCertificate(cert)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as SmartCostApplication
                return FinanceViewModel(
                    application.container.financeRepository,
                    application.container.financialCalculationUseCase
                ) as T
            }
        }
    }
}

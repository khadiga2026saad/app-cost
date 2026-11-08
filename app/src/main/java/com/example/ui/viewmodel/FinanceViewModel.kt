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
import com.example.data.local.entities.GamaeyaInstallmentEntity
import com.example.data.local.entities.RevenueEntity
import com.example.data.local.entities.StudentEntity
import com.example.data.local.entities.VehicleEntity
import com.example.data.local.entities.VehicleFuelLogEntity
import com.example.data.local.entities.VehicleMaintenanceLogEntity
import com.example.data.local.entities.VehicleOilLogEntity
import com.example.data.local.entities.VehicleAccidentLogEntity
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
import kotlinx.coroutines.flow.first
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

    // Complete local records used by the Cost workspace (not limited to the current month).
    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRevenues: StateFlow<List<RevenueEntity>> = repository.getAllRevenues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        repository.getMaintenanceLogsBetween(0L, Long.MAX_VALUE),
        repository.getOilLogsBetween(0L, Long.MAX_VALUE),
        repository.getAllAccidentLogs()
    ) { fuelLogs, maintLogs, oilLogs, accidentLogs ->
        val fuelItems = fuelLogs.map {
            com.example.domain.model.VehicleLogItem(
                id = it.fuelLogId,
                vehicleId = it.vehicleId,
                logType = "FUEL",
                amount = MoneyUtils.formatPiastresPlain(it.totalCostPiastres),
                date = it.date,
                odometer = it.odometerKm.toInt(),
                liters = it.quantityLiters,
                notes = listOf(it.stationName, it.serviceCenter).filterNotNull().filter { value -> value.isNotBlank() }.joinToString(" • ")
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
        val oilItems = oilLogs.map {
            com.example.domain.model.VehicleLogItem(
                id = it.oilLogId,
                vehicleId = it.vehicleId,
                logType = "OIL",
                amount = MoneyUtils.formatPiastresPlain(it.totalCostPiastres),
                date = it.date,
                odometer = it.odometerAtChangeKm.toInt(),
                serviceType = if (it.isFilterReplaced) "زيت / فلتر" else "زيت",
                notes = it.oilBrandAndGrade
            )
        }
        val accidentItems = accidentLogs.map {
            com.example.domain.model.VehicleLogItem(
                id = it.accidentLogId,
                vehicleId = it.vehicleId,
                logType = "ACCIDENT",
                amount = MoneyUtils.formatPiastresPlain(it.netUserCostPiastres),
                date = it.accidentDate,
                odometer = it.odometerKm?.toInt(),
                serviceType = "حادث",
                notes = it.description
            )
        }
        (fuelItems + maintItems + oilItems + accidentItems).sortedByDescending { it.date }
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

    val allTreasuryMovements: StateFlow<List<com.example.data.local.entities.TreasuryMovementEntity>> =
        repository.getAllTreasuryMovements().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
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

    val allUnpaidGamaeyaInstallments: StateFlow<List<GamaeyaInstallmentEntity>> = repository.getAllUnpaidInstallments()
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

    // Reports: compare all twelve months of the year selected in the report filter.
    // Keep the chart/list synchronized when the user navigates to a previous or next year.
    val monthlyReports: StateFlow<List<MonthlyReportItem>> = combine(
        _currentMonthFilter,
        repository.getAllExpenses(),
        repository.getAllRevenues()
    ) { filter, expenses, revenues ->
        val selectedYear = filter.year
        (1..12).map { month ->
            val start = DateUtils.getMonthStartEpoch(selectedYear, month)
            val end = DateUtils.getMonthEndEpoch(selectedYear, month)
            val monthExpenses = expenses.asSequence()
                .filter { it.date in start..end }
                .sumOf { it.amountPiastres }
            val monthRevenues = revenues.asSequence()
                .filter { it.date in start..end && !it.isAccrualCollection }
                .sumOf { it.amountPiastres }
            MonthlyReportItem(
                year = selectedYear,
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
        studentId: Long? = null,
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
                vehicleId = vehicleId,
                studentId = studentId
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
        notes: String = "",
        fuelType: String = "بنزين 95"
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
                    stationName = serviceCenter,
                    fuelType = fuelType,
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

    fun saveOilLog(vehicleId: Long, amount: BigDecimal, date: Long, odometer: Int, brandAndGrade: String, intervalKm: Long, isFilterReplaced: Boolean) {
        viewModelScope.launch {
            val vehicle = repository.getVehicleById(vehicleId) ?: return@launch
            val total = MoneyUtils.egpToPiastres(amount)
            if (total <= 0L || odometer < 0 || intervalKm <= 0L) return@launch
            repository.insertOilLogWithExpense(
                VehicleOilLogEntity(
                    vehicleId = vehicleId, date = date, odometerAtChangeKm = odometer.toLong(),
                    oilBrandAndGrade = brandAndGrade.ifBlank { "زيت / فلتر" }, intervalKm = intervalKm,
                    nextDueOdometerKm = odometer.toLong() + intervalKm, isFilterReplaced = isFilterReplaced,
                    totalCostPiastres = total, linkedExpenseId = 0L
                ), vehicle.name
            )
        }
    }

    fun saveAccidentLog(vehicleId: Long, date: Long, odometer: Int?, description: String, estimatedDamage: BigDecimal, actualRepairCost: BigDecimal, insuranceCompensation: BigDecimal, notes: String = "") {
        viewModelScope.launch {
            val vehicle = repository.getVehicleById(vehicleId) ?: return@launch
            val estimate = MoneyUtils.egpToPiastres(estimatedDamage)
            val actual = MoneyUtils.egpToPiastres(actualRepairCost)
            val insurance = MoneyUtils.egpToPiastres(insuranceCompensation)
            if (estimate < 0L || actual < 0L || insurance < 0L) return@launch
            val net = maxOf(0L, actual - insurance)
            repository.insertAccidentLogWithExpense(
                VehicleAccidentLogEntity(
                    vehicleId = vehicleId, accidentDate = date, odometerKm = odometer?.toLong(),
                    description = listOf(description, notes).filter { it.isNotBlank() }.joinToString(" — ").ifBlank { "حادث مركبة" },
                    estimatedDamagePiastres = estimate, actualRepairCostPiastres = actual,
                    insuranceCompensationPiastres = insurance, netUserCostPiastres = net,
                    compensationStatus = when { actual == 0L -> "PENDING_CLAIM"; insurance >= actual -> "COMPENSATED"; else -> "PENDING_CLAIM" }
                ), vehicle.name
            )
        }
    }

    fun deleteVehicleLog(id: Long, logType: String) {
        viewModelScope.launch {
            repository.deleteVehicleLogById(id, logType)
        }
    }

    fun recordTreasuryMovement(type: String, amount: BigDecimal, reason: String, date: Long = DateUtils.currentEpochMillis()) {
        if (amount <= BigDecimal.ZERO) return
        viewModelScope.launch {
            repository.recordTreasuryMovement(type, MoneyUtils.egpToPiastres(amount), reason.ifBlank { "حركة حساب جاري" }, date)
        }
    }

    fun deleteStudent(id: Long) {
        viewModelScope.launch {
            val student = repository.getStudentById(id) ?: return@launch
            val hasLinkedExpenses = repository.getExpensesByStudent(id).first().isNotEmpty()
            if (!hasLinkedExpenses) repository.deleteStudent(student)
        }
    }

    fun saveStudent(id: Long = 0, fullName: String, academicStage: String, academicYear: String = DateUtils.currentYear().toString() + "/" + (DateUtils.currentYear() + 1).toString()) {
        viewModelScope.launch {
            if (fullName.isBlank() || academicStage.isBlank()) return@launch
            val student = StudentEntity(studentId = id, fullName = fullName.trim(), academicStage = academicStage, academicYear = academicYear)
            if (id == 0L) repository.insertStudent(student) else repository.updateStudent(student)
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

    // Gamaeyat: create the circle and its scheduled installments atomically in Room.
    fun payGamaeyaInstallment(installmentId: Long) {
        viewModelScope.launch {
            repository.payGamaeyaInstallment(installmentId, DateUtils.currentEpochMillis())
        }
    }

    fun collectGamaeyaPayout(gamaeyaId: Long) {
        viewModelScope.launch {
            repository.collectGamaeyaPayout(gamaeyaId, DateUtils.currentEpochMillis())
        }
    }

    fun saveGamaeya(
        name: String,
        totalPoolAmount: BigDecimal,
        numberOfMembers: Int,
        monthlyInstallment: BigDecimal,
        myTurnOrder: Int,
        startDate: Long,
        expectedPayoutDate: Long
    ) {
        viewModelScope.launch {
            if (name.isBlank() || numberOfMembers <= 0 || myTurnOrder !in 1..numberOfMembers) return@launch
            val totalPoolPiastres = MoneyUtils.egpToPiastres(totalPoolAmount)
            val installmentPiastres = MoneyUtils.egpToPiastres(monthlyInstallment)
            if (totalPoolPiastres <= 0L || installmentPiastres <= 0L || expectedPayoutDate < startDate) return@launch
            repository.createGamaeyaWithInstallments(
                GamaeyaEntity(
                    name = name.trim(),
                    totalPoolValuePiastres = totalPoolPiastres,
                    numberOfMembers = numberOfMembers,
                    monthlyInstallmentPiastres = installmentPiastres,
                    myTurnOrder = myTurnOrder,
                    startDate = startDate,
                    expectedPayoutDate = expectedPayoutDate
                )
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
        notes: String = "",
        durationYears: Int = 3
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
                durationYears = durationYears.coerceAtLeast(1),
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

    val allUncollectedCertificateSchedules: StateFlow<List<com.example.data.local.entities.CertificatePayoutScheduleEntity>> =
        repository.getDueUncollectedSchedules(DateUtils.currentEpochMillis()).stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun collectCertificatePayout(scheduleId: Long, collectedDate: Long) {
        viewModelScope.launch { repository.collectCertificatePayout(scheduleId, collectedDate) }
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

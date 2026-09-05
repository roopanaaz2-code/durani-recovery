package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ScanReportEntity
import com.example.data.model.DeviceStorageStats
import com.example.data.model.MediaCandidate
import com.example.data.model.MediaType
import com.example.data.model.RecoveryOperationResult
import com.example.data.model.RecoveryStatus
import com.example.data.model.ScanMode
import com.example.data.model.ScanStats
import com.example.data.repository.RecoveryRepository
import com.example.engine.RealRecoveryEngine
import com.example.engine.StorageInspector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecoveryRepository
    private val recoveryEngine = RealRecoveryEngine()

    init {
        val db = AppDatabase.getInstance(application)
        repository = RecoveryRepository(db.scanReportDao())
    }

    val scanHistory: StateFlow<List<ScanReportEntity>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _deviceStorageStats = MutableStateFlow<DeviceStorageStats?>(null)
    val deviceStorageStats: StateFlow<DeviceStorageStats?> = _deviceStorageStats.asStateFlow()

    private val _scanStats = MutableStateFlow(ScanStats())
    val scanStats: StateFlow<ScanStats> = _scanStats.asStateFlow()

    private val _candidates = MutableStateFlow<List<MediaCandidate>>(emptyList())
    val candidates: StateFlow<List<MediaCandidate>> = _candidates.asStateFlow()

    private val _selectedCandidateIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedCandidateIds: StateFlow<Set<String>> = _selectedCandidateIds.asStateFlow()

    // Filters
    private val _selectedScanMode = MutableStateFlow(ScanMode.QUICK)
    val selectedScanMode: StateFlow<ScanMode> = _selectedScanMode.asStateFlow()

    private val _searchLastTwoYears = MutableStateFlow(false)
    val searchLastTwoYears: StateFlow<Boolean> = _searchLastTwoYears.asStateFlow()

    private val _selectedMediaTypeFilter = MutableStateFlow<MediaType?>(null) // null = ALL
    val selectedMediaTypeFilter: StateFlow<MediaType?> = _selectedMediaTypeFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<RecoveryStatus?>(null)
    val selectedStatusFilter: StateFlow<RecoveryStatus?> = _selectedStatusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered candidates list
    val filteredCandidates: StateFlow<List<MediaCandidate>> = combine(
        _candidates,
        _selectedMediaTypeFilter,
        _selectedStatusFilter,
        _searchQuery
    ) { candidateList, typeFilter, statusFilter, query ->
        candidateList.filter { candidate ->
            val matchesType = typeFilter == null || candidate.mediaType == typeFilter
            val matchesStatus = statusFilter == null || candidate.recoveryStatus == statusFilter
            val matchesQuery = query.isBlank() || candidate.fileName.contains(query, ignoreCase = true) ||
                    (candidate.filePath?.contains(query, ignoreCase = true) == true)
            matchesType && matchesStatus && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preview
    private val _previewCandidate = MutableStateFlow<MediaCandidate?>(null)
    val previewCandidate: StateFlow<MediaCandidate?> = _previewCandidate.asStateFlow()

    // Recovery action state
    private val _isRecovering = MutableStateFlow(false)
    val isRecovering: StateFlow<Boolean> = _isRecovering.asStateFlow()

    private val _recoveryProgressMessage = MutableStateFlow("")
    val recoveryProgressMessage: StateFlow<String> = _recoveryProgressMessage.asStateFlow()

    private val _recoveryResults = MutableStateFlow<List<RecoveryOperationResult>>(emptyList())
    val recoveryResults: StateFlow<List<RecoveryOperationResult>> = _recoveryResults.asStateFlow()

    private val _lastGeneratedReport = MutableStateFlow<ScanReportEntity?>(null)
    val lastGeneratedReport: StateFlow<ScanReportEntity?> = _lastGeneratedReport.asStateFlow()

    private var scanJob: Job? = null

    init {
        refreshStorageInfo()
    }

    fun refreshStorageInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = StorageInspector.inspectDeviceStorage(getApplication())
            _deviceStorageStats.value = stats
        }
    }

    fun setScanMode(mode: ScanMode) {
        if (!_scanStats.value.isRunning) {
            _selectedScanMode.value = mode
        }
    }

    fun setSearchLastTwoYears(enabled: Boolean) {
        _searchLastTwoYears.value = enabled
    }

    fun setMediaTypeFilter(type: MediaType?) {
        _selectedMediaTypeFilter.value = type
    }

    fun setStatusFilter(status: RecoveryStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPreviewCandidate(candidate: MediaCandidate?) {
        _previewCandidate.value = candidate
    }

    fun toggleCandidateSelection(candidateId: String) {
        val current = _selectedCandidateIds.value.toMutableSet()
        if (current.contains(candidateId)) {
            current.remove(candidateId)
        } else {
            current.add(candidateId)
        }
        _selectedCandidateIds.value = current
    }

    fun selectAllFiltered() {
        val currentFiltered = filteredCandidates.value.map { it.id }.toSet()
        _selectedCandidateIds.value = currentFiltered
    }

    fun deselectAll() {
        _selectedCandidateIds.value = emptySet()
    }

    fun startScan() {
        if (scanJob?.isActive == true) return

        _candidates.value = emptyList()
        _selectedCandidateIds.value = emptySet()
        _recoveryResults.value = emptyList()
        _lastGeneratedReport.value = null

        val mode = _selectedScanMode.value
        val twoYearsOnly = _searchLastTwoYears.value

        scanJob = viewModelScope.launch {
            try {
                val finalStats = recoveryEngine.scanStorage(
                    context = getApplication(),
                    scanMode = mode,
                    lastTwoYearsOnly = twoYearsOnly,
                    onStatsUpdate = { stats ->
                        _scanStats.value = stats
                    },
                    onCandidateDiscovered = { candidate ->
                        _candidates.value = _candidates.value + candidate
                    }
                )

                // Save report to Room database
                val report = ScanReportEntity(
                    scanMode = mode.title,
                    durationMs = finalStats.elapsedTimeMs,
                    bytesExamined = finalStats.bytesExamined,
                    totalFound = finalStats.filesDiscovered,
                    recoverableCount = finalStats.recoverableCandidates,
                    corruptedCount = finalStats.corruptedCandidates,
                    inaccessibleRegionsCount = finalStats.skippedRegionsCount,
                    recoveredCount = 0,
                    failedRecoveryCount = 0,
                    storageAnalyzed = if (mode == ScanMode.DEEP_FORENSIC) "Internal + Accessible Public + Caches" else "MediaStore + Caches",
                    summaryDetails = "Scan completed: ${finalStats.recoverableCandidates} recoverable files found out of ${finalStats.filesDiscovered} discovered records."
                )

                val reportId = withContext(Dispatchers.IO) {
                    repository.saveReport(report)
                }
                _lastGeneratedReport.value = report.copy(id = reportId)

            } catch (e: CancellationException) {
                // Cancelled cleanly
            } catch (e: Exception) {
                _scanStats.value = _scanStats.value.copy(
                    currentStage = "Error during scan: ${e.localizedMessage}",
                    isRunning = false
                )
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        scanJob = null
    }

    fun recoverSelectedCandidates() {
        val selectedIds = _selectedCandidateIds.value
        if (selectedIds.isEmpty()) return

        val itemsToRecover = _candidates.value.filter { selectedIds.contains(it.id) }
        if (itemsToRecover.isEmpty()) return

        viewModelScope.launch {
            _isRecovering.value = true
            val results = mutableListOf<RecoveryOperationResult>()
            var successCount = 0
            var failCount = 0

            for ((index, candidate) in itemsToRecover.withIndex()) {
                _recoveryProgressMessage.value = "Recovering ${index + 1}/${itemsToRecover.size}: ${candidate.fileName}"

                val result = recoveryEngine.recoverCandidate(getApplication(), candidate)
                results.add(result)

                if (result.isSuccess) {
                    successCount++
                } else {
                    failCount++
                }

                // Update candidate status in list
                _candidates.value = _candidates.value.map {
                    if (it.id == candidate.id) {
                        it.copy(
                            isRecovered = result.isSuccess,
                            recoveredPath = result.targetPath,
                            recoveryError = result.errorMessage
                        )
                    } else it
                }
            }

            _recoveryResults.value = results
            _isRecovering.value = false
            _recoveryProgressMessage.value = "Recovery complete: $successCount recovered successfully, $failCount failed."

            // Update report if available
            _lastGeneratedReport.value?.let { currentReport ->
                val updatedReport = currentReport.copy(
                    recoveredCount = successCount,
                    failedRecoveryCount = failCount,
                    summaryDetails = currentReport.summaryDetails + " | Recovered: $successCount, Failed: $failCount"
                )
                withContext(Dispatchers.IO) {
                    repository.saveReport(updatedReport)
                }
                _lastGeneratedReport.value = updatedReport
            }
        }
    }

    fun clearReportHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }
}

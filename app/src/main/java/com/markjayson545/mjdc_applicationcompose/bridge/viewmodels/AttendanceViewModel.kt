package com.markjayson545.mjdc_applicationcompose.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CheckIns
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceRepository
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceResult
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceStats
import com.markjayson545.mjdc_applicationcompose.bridge.repository.BulkAttendanceResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for Attendance Management
 *
 * ARCHITECTURE: Uses Repository pattern for business logic separation
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                     ATTENDANCE VIEWMODEL                                │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ observes StateFlow                                            │
 * │  ► AttendanceViewModel ◄ ─── YOU ARE HERE                              │
 * │         ↓ calls repository methods                                      │
 * │  AttendanceRepository (Business Logic)                                  │
 * │         ↓ calls DAO methods                                             │
 * │  CheckInsDao (Data Access)                                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Manage UI state for attendance screens
 * - Delegate business logic to AttendanceRepository
 * - Handle loading states and error messages
 * - Provide reactive data streams to UI
 */
class AttendanceViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    // ========================================================================
    // UI STATE
    // ========================================================================

    private val _checkIns = MutableStateFlow<List<CheckIns>>(emptyList())
    val checkIns: StateFlow<List<CheckIns>> = _checkIns.asStateFlow()

    private val _teacherCheckIns = MutableStateFlow<List<CheckIns>>(emptyList())
    val teacherCheckIns: StateFlow<List<CheckIns>> = _teacherCheckIns.asStateFlow()

    private val _subjectCheckIns = MutableStateFlow<List<CheckIns>>(emptyList())
    val subjectCheckIns: StateFlow<List<CheckIns>> = _subjectCheckIns.asStateFlow()

    private val _todayCheckIns = MutableStateFlow<List<CheckIns>>(emptyList())
    val todayCheckIns: StateFlow<List<CheckIns>> = _todayCheckIns.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    init {
        loadAllCheckIns()
    }

    private fun loadAllCheckIns() {
        viewModelScope.launch {
            repository.getAllCheckIns().collect { checkInList ->
                _checkIns.value = checkInList
            }
        }
    }

    // ========================================================================
    // DATA LOADING OPERATIONS
    // ========================================================================

    /**
     * Loads all check-ins recorded by a specific teacher.
     * Updates [teacherCheckIns] StateFlow.
     */
    fun loadCheckInsByTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.getCheckInsByTeacher(teacherId).collect { checkInList ->
                _teacherCheckIns.value = checkInList
            }
        }
    }

    /**
     * Loads all check-ins for a specific subject.
     * Updates [subjectCheckIns] StateFlow.
     */
    fun loadCheckInsBySubject(subjectId: String) {
        viewModelScope.launch {
            repository.getCheckInsBySubject(subjectId).collect { checkInList ->
                _subjectCheckIns.value = checkInList
            }
        }
    }

    /**
     * Loads today's check-ins for a specific teacher.
     * Updates [todayCheckIns] StateFlow.
     */
    fun loadTodayCheckIns(teacherId: String) {
        viewModelScope.launch {
            repository.getTodayCheckIns(teacherId).collect { checkInList ->
                _todayCheckIns.value = checkInList
            }
        }
    }

    // ========================================================================
    // FLOW ACCESSORS (for direct collection in UI)
    // ========================================================================

    fun getCheckInsByTeacherFlow(teacherId: String): Flow<List<CheckIns>> {
        return repository.getCheckInsByTeacher(teacherId)
    }

    fun getCheckInsBySubjectFlow(subjectId: String): Flow<List<CheckIns>> {
        return repository.getCheckInsBySubject(subjectId)
    }

    fun getCheckInsByStudentFlow(studentId: String): Flow<List<CheckIns>> {
        return repository.getCheckInsByStudent(studentId)
    }

    fun getCheckInsByDateFlow(date: String): Flow<List<CheckIns>> {
        return repository.getCheckInsByDate(date)
    }

    fun getCheckInsBySubjectAndDateFlow(subjectId: String, date: String): Flow<List<CheckIns>> {
        return repository.getCheckInsBySubjectAndDate(subjectId, date)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Records attendance for a single student.
     * Delegates to repository for business logic and validation.
     */
    fun recordAttendance(
        studentId: String,
        subjectId: String,
        teacherId: String,
        status: AttendanceStatus = AttendanceStatus.PRESENT
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.recordAttendance(studentId, subjectId, teacherId, status)) {
                    is AttendanceResult.Success -> {
                        _errorMessage.value = null
                        // Reload today's check-ins to update UI
                        loadTodayCheckIns(teacherId)
                    }
                    is AttendanceResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to record attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Records attendance for multiple students at once.
     * Delegates to repository for bulk operation.
     */
    fun recordBulkAttendance(
        studentIds: List<String>,
        subjectId: String,
        teacherId: String,
        statusMap: Map<String, AttendanceStatus>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.recordBulkAttendance(studentIds, subjectId, teacherId, statusMap)) {
                    is BulkAttendanceResult.Success -> {
                        _errorMessage.value = null
                        // Reload today's check-ins to update UI
                        loadTodayCheckIns(teacherId)
                    }
                    is BulkAttendanceResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to record bulk attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates an existing attendance record.
     */
    fun updateAttendance(checkIn: CheckIns) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateAttendance(checkIn)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes an attendance record.
     */
    fun deleteAttendance(checkIn: CheckIns) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteAttendance(checkIn)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    suspend fun getCheckInById(checkInId: String): CheckIns? {
        return repository.getCheckInById(checkInId)
    }

    suspend fun getCheckInsBySubjectAndDateList(subjectId: String, date: String): List<CheckIns> {
        return repository.getCheckInsBySubjectAndDateList(subjectId, date)
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Gets attendance statistics for a teacher.
     * Returns a Flow that emits AttendanceStats.
     */
    fun getAttendanceStats(teacherId: String): Flow<AttendanceStats> {
        return repository.getAttendanceStats(teacherId)
    }

    // ========================================================================
    // UTILITY
    // ========================================================================

    fun clearError() {
        _errorMessage.value = null
    }

    // ========================================================================
    // FACTORY
    // ========================================================================

    class Factory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
                return AttendanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Convenience factory that creates repository from database.
         * Use this when you don't have a pre-configured repository.
         */
        fun createFactory(database: AttendanceSystemDatabase): ViewModelProvider.Factory {
            val repository = AttendanceRepository(database.checkInsDao())
            return Factory(repository)
        }
    }
}

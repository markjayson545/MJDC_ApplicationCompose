package com.markjayson545.mjdc_applicationcompose.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.bridge.repository.EnrollmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Student-Subject Enrollment Management
 *
 * ARCHITECTURE: Uses Repository pattern for business logic separation
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                     ENROLLMENT VIEWMODEL                                │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ observes StateFlow                                            │
 * │  ► EnrollmentViewModel ◄ ─── YOU ARE HERE                              │
 * │         ↓ calls repository methods                                      │
 * │  EnrollmentRepository (Business Logic)                                  │
 * │         ↓ calls DAO methods                                             │
 * │  StudentSubjectCrossRefDao                                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Manage UI state for enrollment screens
 * - Track enrollment counts for display
 * - Handle enrollment/unenrollment operations
 * - Provide enrolled student filtering for attendance
 */
class EnrollmentViewModel(
    private val repository: EnrollmentRepository
) : ViewModel() {

    // ========================================================================
    // UI STATE
    // ========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Enrollment counts by student ID (for ManageStudents screen)
    private val _studentEnrollmentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val studentEnrollmentCounts: StateFlow<Map<String, Int>> = _studentEnrollmentCounts.asStateFlow()

    // Enrollment counts by subject ID (for ManageSubjects screen)
    private val _subjectEnrollmentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val subjectEnrollmentCounts: StateFlow<Map<String, Int>> = _subjectEnrollmentCounts.asStateFlow()

    // Enrolled subject IDs for a specific student (for editing)
    private val _enrolledSubjectIds = MutableStateFlow<List<String>>(emptyList())
    val enrolledSubjectIds: StateFlow<List<String>> = _enrolledSubjectIds.asStateFlow()

    // Enrolled student IDs for a specific subject (for editing and attendance)
    private val _enrolledStudentIds = MutableStateFlow<List<String>>(emptyList())
    val enrolledStudentIds: StateFlow<List<String>> = _enrolledStudentIds.asStateFlow()

    // ========================================================================
    // LOAD OPERATIONS
    // ========================================================================

    /**
     * Load enrollment counts for a list of students.
     * Call this when ManageStudents screen loads.
     */
    fun loadEnrollmentCountsForStudents(students: List<Student>) {
        viewModelScope.launch {
            try {
                val studentIds = students.map { it.studentId }
                val counts = repository.getEnrollmentCountsForStudents(studentIds)
                _studentEnrollmentCounts.value = counts
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load enrollment counts"
            }
        }
    }

    /**
     * Load enrollment counts for a list of subjects.
     * Call this when ManageSubjects screen loads.
     */
    fun loadEnrollmentCountsForSubjects(subjects: List<Subject>) {
        viewModelScope.launch {
            try {
                val subjectIds = subjects.map { it.subjectId }
                val counts = repository.getEnrollmentCountsForSubjects(subjectIds)
                _subjectEnrollmentCounts.value = counts
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load enrollment counts"
            }
        }
    }

    /**
     * Load enrolled subject IDs for a specific student.
     * Call this when opening student enrollment sheet.
     */
    fun loadEnrolledSubjectsForStudent(studentId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val subjectIds = repository.getEnrolledSubjectIds(studentId)
                _enrolledSubjectIds.value = subjectIds
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load enrollments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load enrolled student IDs for a specific subject.
     * Call this when opening subject enrollment sheet or for attendance filtering.
     */
    fun loadEnrolledStudentsForSubject(subjectId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val studentIds = repository.getEnrolledStudentIds(subjectId)
                _enrolledStudentIds.value = studentIds
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load enrollments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get enrolled student IDs for a subject (suspend function for direct use).
     */
    suspend fun getEnrolledStudentIdsForSubject(subjectId: String): List<String> {
        return repository.getEnrolledStudentIds(subjectId)
    }

    // ========================================================================
    // ENROLLMENT OPERATIONS - FROM STUDENT SIDE
    // ========================================================================

    /**
     * Update a student's subject enrollments.
     * Syncs the database to match the provided subject IDs.
     */
    fun updateStudentEnrollments(studentId: String, subjectIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateStudentEnrollments(studentId, subjectIds)
                _enrolledSubjectIds.value = subjectIds
                // Update the count for this student
                _studentEnrollmentCounts.value = _studentEnrollmentCounts.value.toMutableMap().apply {
                    put(studentId, subjectIds.size)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update enrollments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Enroll a student in a single subject.
     */
    fun enrollStudentInSubject(studentId: String, subjectId: String) {
        viewModelScope.launch {
            try {
                repository.enrollStudent(studentId, subjectId)
                // Update local state
                _enrolledSubjectIds.value = _enrolledSubjectIds.value + subjectId
                _studentEnrollmentCounts.value = _studentEnrollmentCounts.value.toMutableMap().apply {
                    put(studentId, (get(studentId) ?: 0) + 1)
                }
                _subjectEnrollmentCounts.value = _subjectEnrollmentCounts.value.toMutableMap().apply {
                    put(subjectId, (get(subjectId) ?: 0) + 1)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to enroll student"
            }
        }
    }

    /**
     * Unenroll a student from a single subject.
     */
    fun unenrollStudentFromSubject(studentId: String, subjectId: String) {
        viewModelScope.launch {
            try {
                repository.unenrollStudent(studentId, subjectId)
                // Update local state
                _enrolledSubjectIds.value = _enrolledSubjectIds.value - subjectId
                _studentEnrollmentCounts.value = _studentEnrollmentCounts.value.toMutableMap().apply {
                    put(studentId, maxOf((get(studentId) ?: 1) - 1, 0))
                }
                _subjectEnrollmentCounts.value = _subjectEnrollmentCounts.value.toMutableMap().apply {
                    put(subjectId, maxOf((get(subjectId) ?: 1) - 1, 0))
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to unenroll student"
            }
        }
    }

    // ========================================================================
    // ENROLLMENT OPERATIONS - FROM SUBJECT SIDE
    // ========================================================================

    /**
     * Update a subject's student enrollments.
     * Syncs the database to match the provided student IDs.
     */
    fun updateSubjectEnrollments(subjectId: String, studentIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateSubjectEnrollments(subjectId, studentIds)
                _enrolledStudentIds.value = studentIds
                // Update the count for this subject
                _subjectEnrollmentCounts.value = _subjectEnrollmentCounts.value.toMutableMap().apply {
                    put(subjectId, studentIds.size)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update enrollments"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Enroll all provided students in a subject.
     */
    fun enrollAllStudentsInSubject(subjectId: String, studentIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.enrollStudentsInSubject(subjectId, studentIds)
                _enrolledStudentIds.value = studentIds
                _subjectEnrollmentCounts.value = _subjectEnrollmentCounts.value.toMutableMap().apply {
                    put(subjectId, studentIds.size)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to enroll students"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // UTILITY
    // ========================================================================

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearEnrolledSubjectIds() {
        _enrolledSubjectIds.value = emptyList()
    }

    fun clearEnrolledStudentIds() {
        _enrolledStudentIds.value = emptyList()
    }

    // ========================================================================
    // FACTORY
    // ========================================================================

    class Factory(private val repository: EnrollmentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EnrollmentViewModel::class.java)) {
                return EnrollmentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Convenience factory that creates repository from database.
         */
        fun createFactory(database: AttendanceSystemDatabase): ViewModelProvider.Factory {
            val repository = EnrollmentRepository(database.studentSubjectCrossRefDao())
            return Factory(repository)
        }
    }
}


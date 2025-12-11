package com.markjayson545.mjdc_applicationcompose.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Teacher
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherWithAllData
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherWithStudents
import com.markjayson545.mjdc_applicationcompose.bridge.repository.RegistrationResult
import com.markjayson545.mjdc_applicationcompose.bridge.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Authentication result sealed class for UI state management.
 */
sealed class AuthResult {
    data class Success(val teacher: Teacher) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

/**
 * ViewModel for Teacher Management and Authentication
 *
 * ARCHITECTURE: Uses Repository pattern for business logic separation
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      TEACHER VIEWMODEL                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ observes StateFlow                                            │
 * │  ► TeacherViewModel ◄ ─── YOU ARE HERE                                 │
 * │         ↓ calls repository methods                                      │
 * │  TeacherRepository (Business Logic)                                     │
 * │         ↓ calls DAO methods                                             │
 * │  TeacherDao (Data Access)                                               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Manage authentication state (login/logout/register)
 * - Handle current teacher session
 * - Delegate business logic to TeacherRepository
 * - Provide reactive data streams to UI
 */
class TeacherViewModel(
    private val repository: TeacherRepository
) : ViewModel() {

    // ========================================================================
    // UI STATE
    // ========================================================================

    private val _currentTeacher = MutableStateFlow<Teacher?>(null)
    val currentTeacher: StateFlow<Teacher?> = _currentTeacher.asStateFlow()

    private val _authResult = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authResult: StateFlow<AuthResult> = _authResult.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    init {
        loadAllTeachers()
    }

    fun loadAllTeachers() {
        viewModelScope.launch {
            repository.getAllTeachers().collect { teacherList ->
                _teachers.value = teacherList
            }
        }
    }

    // ========================================================================
    // AUTHENTICATION OPERATIONS
    // ========================================================================

    /**
     * Authenticates a teacher with email and password.
     * Delegates credential validation to repository.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            try {
                val teacher = repository.login(email, password)
                if (teacher != null) {
                    _currentTeacher.value = teacher
                    _isLoggedIn.value = true
                    _authResult.value = AuthResult.Success(teacher)
                } else {
                    _authResult.value = AuthResult.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error(e.message ?: "Login failed")
            }
        }
    }

    /**
     * Registers a new teacher account.
     * Delegates validation and creation to repository.
     */
    fun register(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _authResult.value = AuthResult.Loading
            try {
                when (val result = repository.register(
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword
                )) {
                    is RegistrationResult.Success -> {
                        _currentTeacher.value = result.teacher
                        _isLoggedIn.value = true
                        _authResult.value = AuthResult.Success(result.teacher)
                    }
                    is RegistrationResult.Error -> {
                        _authResult.value = AuthResult.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _authResult.value = AuthResult.Error(e.message ?: "Registration failed")
            }
        }
    }

    /**
     * Logs out the current teacher.
     * Clears all session state.
     */
    fun logout() {
        _currentTeacher.value = null
        _isLoggedIn.value = false
        _authResult.value = AuthResult.Idle
    }

    /**
     * Resets auth result to idle state.
     * Use after handling auth events in UI.
     */
    fun resetAuthResult() {
        _authResult.value = AuthResult.Idle
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Gets a teacher by ID as a reactive Flow.
     */
    fun getTeacherById(teacherId: String): Flow<Teacher?> {
        return repository.getTeacherById(teacherId)
    }

    /**
     * Gets a teacher with all assigned students.
     */
    suspend fun getTeacherWithStudents(teacherId: String): TeacherWithStudents? {
        return repository.getTeacherWithStudents(teacherId)
    }

    /**
     * Gets a teacher with all related data (students, courses, subjects).
     */
    suspend fun getTeacherWithAllData(teacherId: String): TeacherWithAllData? {
        return repository.getTeacherWithAllData(teacherId)
    }

    /**
     * Searches teachers by name.
     */
    fun searchTeachers(query: String): Flow<List<Teacher>> {
        return repository.searchTeachers(query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Updates teacher information.
     * Updates current teacher state if it's the same teacher.
     */
    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            if (_currentTeacher.value?.teacherId == teacher.teacherId) {
                _currentTeacher.value = teacher
            }
        }
    }

    /**
     * Deletes a teacher from the system.
     * Logs out if deleting current teacher.
     */
    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            if (_currentTeacher.value?.teacherId == teacher.teacherId) {
                logout()
            }
        }
    }

    // ========================================================================
    // FACTORY
    // ========================================================================

    class Factory(private val repository: TeacherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TeacherViewModel::class.java)) {
                return TeacherViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Convenience factory that creates repository from database.
         */
        fun createFactory(database: AttendanceSystemDatabase): ViewModelProvider.Factory {
            val repository = TeacherRepository(database.teacherDao())
            return Factory(repository)
        }
    }
}

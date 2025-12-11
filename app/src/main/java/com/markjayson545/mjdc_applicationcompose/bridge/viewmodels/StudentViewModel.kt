package com.markjayson545.mjdc_applicationcompose.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithAttendance
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithTeachers
import com.markjayson545.mjdc_applicationcompose.bridge.repository.StudentCreationResult
import com.markjayson545.mjdc_applicationcompose.bridge.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Student Management
 *
 * ARCHITECTURE: Uses Repository pattern for business logic separation
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      STUDENT VIEWMODEL                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ observes StateFlow                                            │
 * │  ► StudentViewModel ◄ ─── YOU ARE HERE                                 │
 * │         ↓ calls repository methods                                      │
 * │  StudentRepository (Business Logic)                                     │
 * │         ↓ calls DAO methods                                             │
 * │  StudentDao + TeacherStudentCrossRefDao                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Manage UI state for student screens
 * - Delegate business logic to StudentRepository
 * - Handle loading states and error messages
 * - Provide reactive data streams to UI
 */
class StudentViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    // ========================================================================
    // UI STATE
    // ========================================================================

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _teacherStudents = MutableStateFlow<List<Student>>(emptyList())
    val teacherStudents: StateFlow<List<Student>> = _teacherStudents.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    init {
        loadAllStudents()
    }

    private fun loadAllStudents() {
        viewModelScope.launch {
            repository.getAllStudents().collect { studentList ->
                _students.value = studentList
            }
        }
    }

    // ========================================================================
    // DATA LOADING OPERATIONS
    // ========================================================================

    /**
     * Loads students assigned to a specific teacher.
     * Updates [teacherStudents] StateFlow.
     */
    fun loadStudentsByTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.getStudentsByTeacher(teacherId).collect { studentList ->
                _teacherStudents.value = studentList
            }
        }
    }

    // ========================================================================
    // FLOW ACCESSORS (for direct collection in UI)
    // ========================================================================

    fun getStudentsByTeacherFlow(teacherId: String): Flow<List<Student>> {
        return repository.getStudentsByTeacher(teacherId)
    }

    fun getStudentsByCourseFlow(courseId: String): Flow<List<Student>> {
        return repository.getStudentsByCourse(courseId)
    }

    fun getStudentByIdFlow(studentId: String): Flow<Student?> {
        return repository.getStudentByIdFlow(studentId)
    }

    fun searchStudents(query: String): Flow<List<Student>> {
        return repository.searchStudents(query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Creates a new student and assigns to teacher.
     * Delegates validation and creation to repository.
     */
    fun addStudent(
        firstName: String,
        middleName: String,
        lastName: String,
        courseId: String?,
        teacherId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.createStudent(
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    courseId = courseId,
                    teacherId = teacherId
                )) {
                    is StudentCreationResult.Success -> {
                        _errorMessage.value = null
                        // Reload students to update UI
                        loadStudentsByTeacher(teacherId)
                    }
                    is StudentCreationResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add student"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates student information.
     */
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateStudent(student)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update student"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a student from the system.
     */
    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteStudent(student)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete student"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // TEACHER ASSIGNMENT OPERATIONS
    // ========================================================================

    /**
     * Assigns a student to an additional teacher.
     */
    fun assignStudentToTeacher(studentId: String, teacherId: String) {
        viewModelScope.launch {
            try {
                repository.assignStudentToTeacher(studentId, teacherId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to assign student to teacher"
            }
        }
    }

    /**
     * Removes a student from a teacher's roster.
     */
    fun removeStudentFromTeacher(studentId: String, teacherId: String) {
        viewModelScope.launch {
            try {
                repository.removeStudentFromTeacher(studentId, teacherId)
                // Reload students to update UI
                loadStudentsByTeacher(teacherId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to remove student from teacher"
            }
        }
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Sets the currently selected student.
     */
    fun selectStudent(student: Student?) {
        _selectedStudent.value = student
    }

    suspend fun getStudentById(studentId: String): Student? {
        return repository.getStudentById(studentId)
    }

    suspend fun getStudentWithTeachers(studentId: String): StudentWithTeachers? {
        return repository.getStudentWithTeachers(studentId)
    }

    suspend fun getStudentWithAttendance(studentId: String): StudentWithAttendance? {
        return repository.getStudentWithAttendance(studentId)
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

    class Factory(private val repository: StudentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
                return StudentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Convenience factory that creates repository from database.
         */
        fun createFactory(database: AttendanceSystemDatabase): ViewModelProvider.Factory {
            val repository = StudentRepository(
                database.studentDao(),
                database.teacherStudentCrossRefDao()
            )
            return Factory(repository)
        }
    }
}

package com.markjayson545.mjdc_applicationcompose.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.SubjectWithCourses
import com.markjayson545.mjdc_applicationcompose.bridge.repository.SubjectCreationResult
import com.markjayson545.mjdc_applicationcompose.bridge.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Subject Management
 *
 * ARCHITECTURE: Uses Repository pattern for business logic separation
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      SUBJECT VIEWMODEL                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ observes StateFlow                                            │
 * │  ► SubjectViewModel ◄ ─── YOU ARE HERE                                 │
 * │         ↓ calls repository methods                                      │
 * │  SubjectRepository (Business Logic)                                     │
 * │         ↓ calls DAO methods                                             │
 * │  SubjectDao + CourseSubjectCrossRefDao                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Manage UI state for subject screens
 * - Delegate business logic to SubjectRepository
 * - Handle course-subject assignments
 * - Provide reactive data streams to UI
 */
class SubjectViewModel(
    private val repository: SubjectRepository
) : ViewModel() {

    // ========================================================================
    // UI STATE
    // ========================================================================

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _teacherSubjects = MutableStateFlow<List<Subject>>(emptyList())
    val teacherSubjects: StateFlow<List<Subject>> = _teacherSubjects.asStateFlow()

    private val _courseSubjects = MutableStateFlow<List<Subject>>(emptyList())
    val courseSubjects: StateFlow<List<Subject>> = _courseSubjects.asStateFlow()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    private val _selectedSubjectWithCourses = MutableStateFlow<SubjectWithCourses?>(null)
    val selectedSubjectWithCourses: StateFlow<SubjectWithCourses?> = _selectedSubjectWithCourses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    init {
        loadAllSubjects()
    }

    private fun loadAllSubjects() {
        viewModelScope.launch {
            repository.getAllSubjects().collect { subjectList ->
                _subjects.value = subjectList
            }
        }
    }

    // ========================================================================
    // DATA LOADING OPERATIONS
    // ========================================================================

    /**
     * Loads subjects owned by a specific teacher.
     * Updates [teacherSubjects] StateFlow.
     */
    fun loadSubjectsByTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.getSubjectsByTeacher(teacherId).collect { subjectList ->
                _teacherSubjects.value = subjectList
            }
        }
    }

    /**
     * Loads subjects assigned to a specific course.
     * Updates [courseSubjects] StateFlow.
     */
    fun loadSubjectsByCourse(courseId: String) {
        viewModelScope.launch {
            repository.getSubjectsByCourse(courseId).collect { subjectList ->
                _courseSubjects.value = subjectList
            }
        }
    }

    // ========================================================================
    // FLOW ACCESSORS (for direct collection in UI)
    // ========================================================================

    fun getSubjectsByTeacherFlow(teacherId: String): Flow<List<Subject>> {
        return repository.getSubjectsByTeacher(teacherId)
    }

    fun getSubjectsByCourseFlow(courseId: String): Flow<List<Subject>> {
        return repository.getSubjectsByCourse(courseId)
    }

    fun getSubjectByIdFlow(subjectId: String): Flow<Subject?> {
        return repository.getSubjectByIdFlow(subjectId)
    }

    fun getSubjectWithCoursesFlow(subjectId: String): Flow<SubjectWithCourses?> {
        return repository.getSubjectWithCoursesFlow(subjectId)
    }

    fun searchSubjects(query: String): Flow<List<Subject>> {
        return repository.searchSubjects(query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Creates a new subject.
     * Delegates validation and creation to repository.
     */
    fun addSubject(
        subjectName: String,
        subjectCode: String,
        description: String = "",
        teacherId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.createSubject(
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    description = description,
                    teacherId = teacherId
                )) {
                    is SubjectCreationResult.Success -> {
                        _errorMessage.value = null
                        // Reload subjects to update UI
                        loadSubjectsByTeacher(teacherId)
                    }
                    is SubjectCreationResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add subject"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new subject and assigns it to courses.
     */
    fun addSubjectWithCourses(
        subjectName: String,
        subjectCode: String,
        description: String = "",
        teacherId: String,
        courseIds: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.createSubjectWithCourses(
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    description = description,
                    teacherId = teacherId,
                    courseIds = courseIds
                )) {
                    is SubjectCreationResult.Success -> {
                        _errorMessage.value = null
                        // Reload subjects to update UI
                        loadSubjectsByTeacher(teacherId)
                    }
                    is SubjectCreationResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add subject with courses"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates subject information.
     */
    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateSubject(subject)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update subject"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a subject from the system.
     */
    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSubject(subject)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete subject"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // COURSE ASSIGNMENT OPERATIONS
    // ========================================================================

    /**
     * Assigns a subject to a course.
     */
    fun assignSubjectToCourse(subjectId: String, courseId: String) {
        viewModelScope.launch {
            try {
                repository.assignSubjectToCourse(subjectId, courseId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to assign subject to course"
            }
        }
    }

    /**
     * Removes a subject from a course.
     */
    fun removeSubjectFromCourse(subjectId: String, courseId: String) {
        viewModelScope.launch {
            try {
                repository.removeSubjectFromCourse(subjectId, courseId)
                // Reload course subjects
                loadSubjectsByCourse(courseId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to remove subject from course"
            }
        }
    }

    /**
     * Updates all course assignments for a subject.
     */
    fun updateSubjectCourses(subjectId: String, courseIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateSubjectCourses(subjectId, courseIds)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update subject courses"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Sets the currently selected subject and loads its courses.
     */
    fun selectSubject(subject: Subject?) {
        _selectedSubject.value = subject
        subject?.let {
            viewModelScope.launch {
                repository.getSubjectWithCoursesFlow(it.subjectId).collect { subjectWithCourses ->
                    _selectedSubjectWithCourses.value = subjectWithCourses
                }
            }
        }
    }

    suspend fun getSubjectById(subjectId: String): Subject? {
        return repository.getSubjectById(subjectId)
    }

    suspend fun getSubjectWithCourses(subjectId: String): SubjectWithCourses? {
        return repository.getSubjectWithCourses(subjectId)
    }

    suspend fun getCourseIdsForSubject(subjectId: String): List<String> {
        return repository.getCourseIdsForSubject(subjectId)
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

    class Factory(private val repository: SubjectRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
                return SubjectViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Convenience factory that creates repository from database.
         */
        fun createFactory(database: AttendanceSystemDatabase): ViewModelProvider.Factory {
            val repository = SubjectRepository(
                database.subjectDao(),
                database.courseSubjectCrossRefDao()
            )
            return Factory(repository)
        }
    }
}

package com.markjayson545.mjdc_applicationcompose.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CourseWithSubjects
import com.markjayson545.mjdc_applicationcompose.bridge.repository.CourseCreationResult
import com.markjayson545.mjdc_applicationcompose.bridge.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Course Management
 *
 * ARCHITECTURE: Uses Repository pattern for business logic separation
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      COURSE VIEWMODEL                                   │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ observes StateFlow                                            │
 * │  ► CourseViewModel ◄ ─── YOU ARE HERE                                  │
 * │         ↓ calls repository methods                                      │
 * │  CourseRepository (Business Logic)                                      │
 * │         ↓ calls DAO methods                                             │
 * │  CourseDao (Data Access)                                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Manage UI state for course screens
 * - Delegate business logic to CourseRepository
 * - Handle loading states and error messages
 * - Provide reactive data streams to UI
 */
class CourseViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    // ========================================================================
    // UI STATE
    // ========================================================================

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _teacherCourses = MutableStateFlow<List<Course>>(emptyList())
    val teacherCourses: StateFlow<List<Course>> = _teacherCourses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    private val _selectedCourseWithSubjects = MutableStateFlow<CourseWithSubjects?>(null)
    val selectedCourseWithSubjects: StateFlow<CourseWithSubjects?> = _selectedCourseWithSubjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    init {
        loadAllCourses()
    }

    private fun loadAllCourses() {
        viewModelScope.launch {
            repository.getAllCourses().collect { courseList ->
                _courses.value = courseList
            }
        }
    }

    // ========================================================================
    // DATA LOADING OPERATIONS
    // ========================================================================

    /**
     * Loads courses owned by a specific teacher.
     * Updates [teacherCourses] StateFlow.
     */
    fun loadCoursesByTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.getCoursesByTeacher(teacherId).collect { courseList ->
                _teacherCourses.value = courseList
            }
        }
    }

    // ========================================================================
    // FLOW ACCESSORS (for direct collection in UI)
    // ========================================================================

    fun getCoursesByTeacherFlow(teacherId: String): Flow<List<Course>> {
        return repository.getCoursesByTeacher(teacherId)
    }

    fun getCoursesWithSubjectsByTeacherFlow(teacherId: String): Flow<List<CourseWithSubjects>> {
        return repository.getCoursesWithSubjectsByTeacher(teacherId)
    }

    fun getCourseByIdFlow(courseId: String): Flow<Course?> {
        return repository.getCourseByIdFlow(courseId)
    }

    fun getCourseWithSubjectsFlow(courseId: String): Flow<CourseWithSubjects?> {
        return repository.getCourseWithSubjectsFlow(courseId)
    }

    fun searchCourses(query: String): Flow<List<Course>> {
        return repository.searchCourses(query)
    }

    fun searchCoursesByTeacher(teacherId: String, query: String): Flow<List<Course>> {
        return repository.searchCoursesByTeacher(teacherId, query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Creates a new course.
     * Delegates validation and creation to repository.
     */
    fun addCourse(
        courseName: String,
        courseCode: String,
        teacherId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.createCourse(
                    courseName = courseName,
                    courseCode = courseCode,
                    teacherId = teacherId
                )) {
                    is CourseCreationResult.Success -> {
                        _errorMessage.value = null
                        // Reload courses to update UI
                        loadCoursesByTeacher(teacherId)
                    }
                    is CourseCreationResult.Error -> {
                        _errorMessage.value = result.message
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to add course"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates course information.
     */
    fun updateCourse(course: Course) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourse(course)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update course"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a course from the system.
     */
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteCourse(course)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete course"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Sets the currently selected course and loads its subjects.
     */
    fun selectCourse(course: Course?) {
        _selectedCourse.value = course
        course?.let {
            viewModelScope.launch {
                repository.getCourseWithSubjectsFlow(it.courseId).collect { courseWithSubjects ->
                    _selectedCourseWithSubjects.value = courseWithSubjects
                }
            }
        }
    }

    suspend fun getCourseById(courseId: String): Course? {
        return repository.getCourseById(courseId)
    }

    suspend fun getCourseWithSubjects(courseId: String): CourseWithSubjects? {
        return repository.getCourseWithSubjects(courseId)
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

    class Factory(private val repository: CourseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
                return CourseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        /**
         * Convenience factory that creates repository from database.
         */
        fun createFactory(database: AttendanceSystemDatabase): ViewModelProvider.Factory {
            val repository = CourseRepository(database.courseDao())
            return Factory(repository)
        }
    }
}

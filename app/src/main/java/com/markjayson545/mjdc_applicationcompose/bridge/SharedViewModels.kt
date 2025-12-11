package com.markjayson545.mjdc_applicationcompose.bridge

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.AttendanceViewModel
import com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.CourseViewModel
import com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.EnrollmentViewModel
import com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.StudentViewModel
import com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.SubjectViewModel
import com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.TeacherViewModel

/**
 * SharedViewModels aggregates all individual ViewModels for the attendance system.
 * This allows screens to access a single source of truth for all data operations.
 *
 * ARCHITECTURE: Implements MVVM with Repository pattern
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                     SHARED VIEWMODELS                                   │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  UI Layer (Compose Screens)                                             │
 * │         ↓ accesses SharedViewModels                                     │
 * │  ► SharedViewModels ◄ ─── YOU ARE HERE                                 │
 * │         ↓ provides individual ViewModels                                │
 * │  Individual ViewModels (Teacher, Student, Course, Subject, Attendance) │
 * │         ↓ use Repositories                                              │
 * │  Repositories (Business Logic)                                          │
 * │         ↓ use DAOs                                                      │
 * │  Room Database                                                          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * RESPONSIBILITIES:
 * - Provide centralized access to all ViewModels
 * - Manage RepositoryProvider for dependency injection
 * - Handle cross-ViewModel initialization
 * - Provide singleton access pattern
 */
class SharedViewModels(
    private val database: AttendanceSystemDatabase
) : ViewModel() {

    // Repository provider for dependency injection
    private val repositoryProvider = RepositoryProvider(database)

    // ========================================================================
    // VIEW MODELS
    // ========================================================================

    val teacherViewModel: TeacherViewModel by lazy {
        TeacherViewModel(repositoryProvider.teacherRepository)
    }

    val studentViewModel: StudentViewModel by lazy {
        StudentViewModel(repositoryProvider.studentRepository)
    }

    val courseViewModel: CourseViewModel by lazy {
        CourseViewModel(repositoryProvider.courseRepository)
    }

    val subjectViewModel: SubjectViewModel by lazy {
        SubjectViewModel(repositoryProvider.subjectRepository)
    }

    val attendanceViewModel: AttendanceViewModel by lazy {
        AttendanceViewModel(repositoryProvider.attendanceRepository)
    }

    val enrollmentViewModel: EnrollmentViewModel by lazy {
        EnrollmentViewModel(repositoryProvider.enrollmentRepository)
    }

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    /**
     * Initialize data loading for a specific teacher after login.
     * Loads all teacher-specific data across all ViewModels.
     */
    fun initializeForTeacher(teacherId: String) {
        studentViewModel.loadStudentsByTeacher(teacherId)
        courseViewModel.loadCoursesByTeacher(teacherId)
        subjectViewModel.loadSubjectsByTeacher(teacherId)
        attendanceViewModel.loadCheckInsByTeacher(teacherId)
        attendanceViewModel.loadTodayCheckIns(teacherId)
    }

    /**
     * Clear all cached data on logout.
     * Resets teacher session and clears sensitive data.
     */
    fun clearAllData() {
        teacherViewModel.logout()
    }

    // ========================================================================
    // FACTORY
    // ========================================================================

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SharedViewModels::class.java)) {
                val database = AttendanceSystemDatabase.getInstance(context)
                return SharedViewModels(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // ========================================================================
    // SINGLETON ACCESS
    // ========================================================================

    companion object {
        @Volatile
        private var INSTANCE: SharedViewModels? = null

        /**
         * Gets or creates the SharedViewModels singleton instance.
         * Uses double-checked locking for thread safety.
         */
        fun getInstance(context: Context): SharedViewModels {
            return INSTANCE ?: synchronized(this) {
                val database = AttendanceSystemDatabase.getInstance(context)
                val instance = SharedViewModels(database)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clears the singleton instance.
         * Use when app needs to reset completely (e.g., logout).
         */
        fun clearInstance() {
            synchronized(this) {
                INSTANCE = null
            }
        }
    }
}
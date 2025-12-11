package com.markjayson545.mjdc_applicationcompose.bridge

import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceRepository
import com.markjayson545.mjdc_applicationcompose.bridge.repository.CourseRepository
import com.markjayson545.mjdc_applicationcompose.bridge.repository.EnrollmentRepository
import com.markjayson545.mjdc_applicationcompose.bridge.repository.StudentRepository
import com.markjayson545.mjdc_applicationcompose.bridge.repository.SubjectRepository
import com.markjayson545.mjdc_applicationcompose.bridge.repository.TeacherRepository

/**
 * ============================================================================
 * REPOSITORY PROVIDER
 * ============================================================================
 *
 * Centralized factory for all repository instances in the attendance system.
 * This class provides a single point of access to all repositories, ensuring
 * consistent instantiation and dependency injection.
 *
 * ARCHITECTURE LAYER:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        APPLICATION ARCHITECTURE                         │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │   UI (Screens/Composables)                                              │
 * │         ↓                                                               │
 * │   ViewModels (State Management)                                         │
 * │         ↓                                                               │
 * │   ► Repositories (Business Logic) ◄ ─── YOU ARE HERE                    │
 * │         ↓                                                               │
 * │   DAOs (Data Access Objects)                                            │
 * │         ↓                                                               │
 * │   Room Database (SQLite)                                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * BENEFITS OF REPOSITORY PATTERN:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. SEPARATION OF CONCERNS                                               │
 * │    - Business logic is isolated from UI and data layers                 │
 * │    - ViewModels only handle state; repositories handle logic            │
 * │                                                                         │
 * │ 2. TESTABILITY                                                          │
 * │    - Repositories can be easily mocked for unit testing                 │
 * │    - Business rules can be tested independently                         │
 * │                                                                         │
 * │ 3. MAINTAINABILITY                                                      │
 * │    - Changes to business rules affect only repository layer             │
 * │    - Data source changes don't affect business logic                    │
 * │                                                                         │
 * │ 4. REUSABILITY                                                          │
 * │    - Same business logic can be used by multiple ViewModels             │
 * │    - Repositories can be shared across features                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * USAGE:
 * ```kotlin
 * val database = AttendanceSystemDatabase.getInstance(context)
 * val repositories = RepositoryProvider(database)
 *
 * // Access repositories
 * val teacherRepo = repositories.teacherRepository
 * val studentRepo = repositories.studentRepository
 * ```
 *
 * @param database The Room database instance
 */
class RepositoryProvider(
    private val database: AttendanceSystemDatabase
) {

    /**
     * Repository for teacher-related business logic.
     *
     * HANDLES:
     * - Teacher authentication (login/register)
     * - Teacher profile management
     * - Teacher data access
     *
     * @see com.markjayson545.mjdc_applicationcompose.bridge.repository.TeacherRepository
     */
    val teacherRepository: TeacherRepository by lazy {
        TeacherRepository(database.teacherDao())
    }

    /**
     * Repository for student-related business logic.
     *
     * HANDLES:
     * - Student registration and management
     * - Teacher-student assignments
     * - Course enrollment
     *
     * @see com.markjayson545.mjdc_applicationcompose.bridge.repository.StudentRepository
     */
    val studentRepository: StudentRepository by lazy {
        StudentRepository(
            database.studentDao(),
            database.teacherStudentCrossRefDao()
        )
    }

    /**
     * Repository for course-related business logic.
     *
     * HANDLES:
     * - Course creation and management
     * - Course-teacher ownership
     * - Course searching
     *
     * @see com.markjayson545.mjdc_applicationcompose.bridge.repository.CourseRepository
     */
    val courseRepository: CourseRepository by lazy {
        CourseRepository(database.courseDao())
    }

    /**
     * Repository for subject-related business logic.
     *
     * HANDLES:
     * - Subject creation and management
     * - Course-subject assignments
     * - Subject curriculum organization
     *
     * @see com.markjayson545.mjdc_applicationcompose.bridge.repository.SubjectRepository
     */
    val subjectRepository: SubjectRepository by lazy {
        SubjectRepository(
            database.subjectDao(),
            database.courseSubjectCrossRefDao()
        )
    }

    /**
     * Repository for attendance-related business logic.
     *
     * HANDLES:
     * - Recording attendance
     * - Attendance queries and filtering
     * - Statistical reporting
     *
     * @see com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceRepository
     */
    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(database.checkInsDao())
    }

    /**
     * Repository for enrollment-related business logic.
     *
     * HANDLES:
     * - Student-subject enrollment management
     * - Bulk enrollment operations
     * - Enrollment queries for attendance filtering
     *
     * @see com.markjayson545.mjdc_applicationcompose.bridge.repository.EnrollmentRepository
     */
    val enrollmentRepository: EnrollmentRepository by lazy {
        EnrollmentRepository(database.studentSubjectCrossRefDao())
    }
}

/**
 * ============================================================================
 * BUSINESS LOGIC SUMMARY
 * ============================================================================
 *
 * This section provides a quick reference for all business rules implemented
 * across the repository layer.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      AUTHENTICATION & SECURITY                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ • Unique email enforcement for registration                             │
 * │ • Minimum password length: 6 characters                                 │
 * │ • Password confirmation matching                                        │
 * │ • Email format validation (must contain @)                              │
 * │ • No password recovery (security by design)                             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                         OWNERSHIP & ACCESS                              │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ • Courses are owned by single teacher (1:N relationship)               │
 * │ • Subjects are owned by single teacher (1:N relationship)               │
 * │ • Students can be assigned to multiple teachers (N:M)                   │
 * │ • Teachers can only modify their own resources                          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                          ATTENDANCE RULES                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ • 4 status types: PRESENT, ABSENT, LATE, EXCUSED                        │
 * │ • Attendance is per-subject, not per-course                             │
 * │ • Multiple attendance records per day allowed (different subjects)      │
 * │ • Teacher ID recorded for audit trail                                   │
 * │ • Timestamp recorded for each check-in                                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                         CASCADE DELETIONS                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ Teacher deleted → Students unassigned, Courses deleted,                 │
 * │                   Subjects deleted, Attendance deleted                  │
 * │                                                                         │
 * │ Student deleted → All attendance records deleted                        │
 * │                                                                         │
 * │ Course deleted  → Subject associations removed,                         │
 * │                   Students unenrolled                                   │
 * │                                                                         │
 * │ Subject deleted → Course associations removed,                          │
 * │                   Attendance records deleted                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                          VALIDATION RULES                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ All Entities:                                                           │
 * │   • UUID generated automatically for IDs                                │
 * │   • Names trimmed of whitespace                                         │
 * │   • Codes converted to uppercase                                        │
 * │                                                                         │
 * │ Required Fields:                                                        │
 * │   • Teacher: firstName, lastName, email, password                       │
 * │   • Student: firstName, lastName, teacherId                             │
 * │   • Course: courseName, courseCode, teacherId                           │
 * │   • Subject: subjectName, subjectCode, teacherId                        │
 * │   • CheckIn: studentId, subjectId, teacherId                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
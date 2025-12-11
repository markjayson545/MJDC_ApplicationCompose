package com.markjayson545.mjdc_applicationcompose.bridge.repository

import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.CourseDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CourseWithSubjects
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * ============================================================================
 * COURSE REPOSITORY
 * ============================================================================
 *
 * This repository manages all business logic related to course management
 * in the attendance system. Courses are owned by teachers and can have
 * multiple subjects assigned to them.
 *
 * KEY FEATURES:
 * - Course creation and management
 * - Teacher-course ownership
 * - Subject association (many-to-many)
 * - Student enrollment tracking
 *
 * ENTITY RELATIONSHIPS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        COURSE RELATIONSHIPS                             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │   Teacher ──[1:N]──→ Course (ownership - one teacher per course)        │
 * │   Course ←──[N:M]──→ Subject (via CourseSubjectCrossRef)               │
 * │   Course ←──[1:N]──→ Student (enrollment)                               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * BUSINESS RULES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ COURSE CREATION RULES                                                   │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Creating courses with name and code                          │
 * │ ✓ ALLOWS: Automatic teacher assignment as owner                        │
 * │ ✓ ALLOWS: Optional description field                                   │
 * │ ✗ RESTRICTS: Blank course name                                         │
 * │ ✗ RESTRICTS: Blank course code                                         │
 * │ ✗ RESTRICTS: Course without a teacher owner                            │
 * │ ⚠ NOTE: Course codes are not enforced to be unique (by design)         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ OWNERSHIP RULES                                                         │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Teacher to own multiple courses                              │
 * │ ✓ ALLOWS: Course ownership transfer (by updating teacherId)            │
 * │ ✗ RESTRICTS: Multiple owners per course                                │
 * │ ✗ RESTRICTS: Course without owner                                      │
 * │ ⚠ NOTE: Only the owning teacher can modify/delete the course           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DATA ACCESS RULES                                                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Teachers to view only their own courses                      │
 * │ ✓ ALLOWS: Viewing all courses (for enrollment purposes)                │
 * │ ✓ ALLOWS: Searching courses by name or code                            │
 * │ ✓ ALLOWS: Viewing course with all associated subjects                  │
 * │ ✗ DOES NOT: Allow viewing other teachers' course details (restricted)  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DELETION RULES                                                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ⚠ WARNING: Deleting a course will cascade to:                          │
 * │   - All subject associations (CourseSubjectCrossRef)                   │
 * │   - Student enrollments will be set to null                            │
 * │ ⚠ NOTE: Subjects themselves are NOT deleted                            │
 * │ ⚠ NOTE: Students are NOT deleted (just unenrolled)                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param courseDao Data Access Object for course database operations
 */
class CourseRepository(
    private val courseDao: CourseDao
) {

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Retrieves all courses in the system.
     *
     * USE CASE: Student enrollment, system overview
     * ⚠ CAUTION: Returns all courses regardless of ownership
     *
     * @return Flow of all courses (reactive updates)
     */
    fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses()
    }

    /**
     * Retrieves courses owned by a specific teacher.
     *
     * BUSINESS LOGIC:
     * - Filters courses by teacherId (owner)
     * - Only returns courses where teacher is the owner
     *
     * USE CASE: Teacher's course management, dashboard
     *
     * @param teacherId The teacher's unique identifier
     * @return Flow of courses owned by this teacher
     */
    fun getCoursesByTeacher(teacherId: String): Flow<List<Course>> {
        return courseDao.getCoursesByTeacher(teacherId)
    }

    /**
     * Retrieves courses with their associated subjects.
     *
     * INCLUDES: Course data + list of all subjects assigned to each course
     * USE CASE: Course curriculum view, subject management
     *
     * @param teacherId The teacher's unique identifier
     * @return Flow of courses with subject details
     */
    fun getCoursesWithSubjectsByTeacher(teacherId: String): Flow<List<CourseWithSubjects>> {
        return courseDao.getCoursesWithSubjectsByTeacher(teacherId)
    }

    /**
     * Retrieves a specific course by ID.
     *
     * @param courseId Course's unique identifier
     * @return Course if found, null otherwise
     */
    suspend fun getCourseById(courseId: String): Course? {
        return courseDao.getCourseById(courseId)
    }

    /**
     * Retrieves a course by ID as a reactive Flow.
     *
     * USE CASE: Real-time course detail updates
     *
     * @param courseId Course's unique identifier
     * @return Flow of course (null if not found)
     */
    fun getCourseByIdFlow(courseId: String): Flow<Course?> {
        return courseDao.getCourseByIdFlow(courseId)
    }

    /**
     * Retrieves a course with all its associated subjects.
     *
     * USE CASE: Course detail view with curriculum
     *
     * @param courseId Course's unique identifier
     * @return CourseWithSubjects containing course and subject list
     */
    suspend fun getCourseWithSubjects(courseId: String): CourseWithSubjects? {
        return courseDao.getCourseWithSubjects(courseId)
    }

    /**
     * Retrieves a course with subjects as a reactive Flow.
     *
     * @param courseId Course's unique identifier
     * @return Flow of CourseWithSubjects
     */
    fun getCourseWithSubjectsFlow(courseId: String): Flow<CourseWithSubjects?> {
        return courseDao.getCourseWithSubjectsFlow(courseId)
    }

    /**
     * Searches courses by name or code.
     *
     * ALLOWS: Partial matching on course name or code
     *
     * @param query Search term
     * @return Flow of matching courses
     */
    fun searchCourses(query: String): Flow<List<Course>> {
        return courseDao.searchCourses(query)
    }

    /**
     * Searches courses within a teacher's owned courses.
     *
     * BUSINESS LOGIC: Combines teacher filter with search query
     * USE CASE: Teacher searching their own courses
     *
     * @param teacherId Teacher's unique identifier
     * @param query Search term
     * @return Flow of matching courses owned by teacher
     */
    fun searchCoursesByTeacher(teacherId: String, query: String): Flow<List<Course>> {
        return courseDao.searchCoursesByTeacher(teacherId, query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Creates a new course.
     *
     * BUSINESS LOGIC:
     * 1. Validates required fields
     * 2. Generates unique UUID for course
     * 3. Assigns teacher as owner
     *
     * RESTRICTIONS:
     * - Course name cannot be blank
     * - Course code cannot be blank
     * - Teacher ID is required (ownership)
     *
     * ALLOWS:
     * - Duplicate course codes (different sections)
     * - Same course name for different teachers
     *
     * @param courseName Name of the course (required)
     * @param courseCode Code/identifier for the course (required)
     * @param teacherId Owner teacher's ID (required)
     * @return CourseCreationResult indicating success or failure
     */
    suspend fun createCourse(
        courseName: String,
        courseCode: String,
        teacherId: String
    ): CourseCreationResult {
        // Validation: Required fields
        if (courseName.isBlank()) {
            return CourseCreationResult.Error("Course name is required")
        }
        if (courseCode.isBlank()) {
            return CourseCreationResult.Error("Course code is required")
        }
        if (teacherId.isBlank()) {
            return CourseCreationResult.Error("Teacher assignment is required")
        }

        // Create course with generated ID
        val newCourse = Course(
            courseId = generateCourseId(),
            courseName = courseName.trim(),
            courseCode = courseCode.trim().uppercase(),
            teacherId = teacherId
        )

        courseDao.insertCourse(newCourse)
        return CourseCreationResult.Success(newCourse)
    }

    /**
     * Updates course information.
     *
     * ALLOWS:
     * - Updating course name
     * - Updating course code
     * - Transferring ownership (changing teacherId)
     *
     * DOES NOT:
     * - Change course ID
     * - Modify subject associations (use SubjectRepository)
     *
     * BUSINESS RULE: Only the owning teacher should call this
     * (enforced at ViewModel/UI level)
     *
     * @param course Updated course object
     */
    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course)
    }

    /**
     * Deletes a course from the system.
     *
     * ⚠️ CASCADE EFFECTS:
     * - All CourseSubjectCrossRef records will be deleted
     * - Students enrolled in this course will have courseId set to null
     *
     * DOES NOT AFFECT:
     * - Subjects (remain available for other courses)
     * - Students (remain in system, just unenrolled)
     * - Teacher (remains in system)
     * - Attendance records (remain linked to subjects)
     *
     * BUSINESS RULE: Only the owning teacher should call this
     *
     * @param course Course to delete
     */
    suspend fun deleteCourse(course: Course) {
        courseDao.deleteCourse(course)
    }

    // ========================================================================
    // UTILITY OPERATIONS
    // ========================================================================

    /**
     * Checks if a teacher owns a specific course.
     *
     * USE CASE: Authorization check before update/delete
     *
     * @param courseId Course's unique identifier
     * @param teacherId Teacher's unique identifier
     * @return true if teacher owns the course, false otherwise
     */
    suspend fun isTeacherOwner(courseId: String, teacherId: String): Boolean {
        val course = courseDao.getCourseById(courseId)
        return course?.teacherId == teacherId
    }

    /**
     * Gets the count of courses owned by a teacher.
     *
     * USE CASE: Dashboard statistics
     *
     * @param teacherId Teacher's unique identifier
     * @return Number of courses owned
     */
    suspend fun getCourseCountForTeacher(teacherId: String): Int {
        return courseDao.getCourseCountByTeacher(teacherId)
    }

    /**
     * Generates a unique masked course ID in the format COURSE-XXXX
     *
     * The ID is generated by:
     * 1. Getting the max existing number from COURSE-XXX formatted IDs
     * 2. Incrementing by 1
     * 3. Formatting with zero-padding (3 digits)
     *
     * @return A unique course ID like "COURSE-001", "COURSE-002", etc.
     */
    private suspend fun generateCourseId(): String {
        val maxNumber = courseDao.getMaxCourseIdNumber() ?: 0
        var nextNumber = maxNumber + 1
        var newId = "COURSE-%03d".format(nextNumber)

        // Handle potential conflicts (very unlikely)
        while (courseDao.courseIdExists(newId)) {
            nextNumber++
            newId = "COURSE-%03d".format(nextNumber)
        }
        return newId
    }
}

/**
 * Sealed class representing course creation operation results.
 */
sealed class CourseCreationResult {
    /**
     * Course created successfully.
     * @param course The newly created course
     */
    data class Success(val course: Course) : CourseCreationResult()

    /**
     * Course creation failed.
     * @param message Human-readable error description
     */
    data class Error(val message: String) : CourseCreationResult()
}


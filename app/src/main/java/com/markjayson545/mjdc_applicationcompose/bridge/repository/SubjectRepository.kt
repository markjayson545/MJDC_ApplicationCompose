package com.markjayson545.mjdc_applicationcompose.bridge.repository

import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.CourseSubjectCrossRefDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.SubjectDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CourseSubjectCrossRef
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.SubjectWithCourses
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * ============================================================================
 * SUBJECT REPOSITORY
 * ============================================================================
 *
 * This repository manages all business logic related to subject management
 * in the attendance system. Subjects are academic units that can be assigned
 * to multiple courses and are the primary entity for tracking attendance.
 *
 * KEY FEATURES:
 * - Subject creation and management
 * - Multi-course assignment (many-to-many relationship)
 * - Teacher ownership
 * - Attendance tracking pivot
 *
 * ENTITY RELATIONSHIPS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        SUBJECT RELATIONSHIPS                            │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │   Teacher ──[1:N]──→ Subject (ownership - creator of subject)           │
 * │   Subject ←──[N:M]──→ Course (via CourseSubjectCrossRef)               │
 * │   Subject ←──[1:N]──→ CheckIns (attendance records)                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ATTENDANCE TRACKING:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Subjects are the PRIMARY ENTITY for attendance recording               │
 * │                                                                         │
 * │ CheckIn Record Structure:                                               │
 * │   - studentId  → Who attended                                          │
 * │   - subjectId  → What class/subject they attended                      │
 * │   - teacherId  → Who recorded the attendance                           │
 * │   - date/time  → When they attended                                    │
 * │   - status     → PRESENT | ABSENT | LATE | EXCUSED                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * BUSINESS RULES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ SUBJECT CREATION RULES                                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Creating subjects with name and code                         │
 * │ ✓ ALLOWS: Optional description                                         │
 * │ ✓ ALLOWS: Automatic teacher assignment as owner                        │
 * │ ✗ RESTRICTS: Blank subject name                                        │
 * │ ✗ RESTRICTS: Blank subject code                                        │
 * │ ✗ RESTRICTS: Subject without a teacher owner                           │
 * │ ⚠ NOTE: Subject codes are not enforced to be unique                    │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ COURSE ASSIGNMENT RULES                                                 │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Assigning subject to multiple courses                        │
 * │ ✓ ALLOWS: Same subject in different courses (shared curriculum)        │
 * │ ✓ ALLOWS: Removing subject from a course                               │
 * │ ✓ ALLOWS: Bulk updating course assignments                             │
 * │ ✗ RESTRICTS: Duplicate subject-course assignments                      │
 * │ ⚠ NOTE: Subject can exist without course (standalone)                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DATA ACCESS RULES                                                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Teachers to view only their own subjects                     │
 * │ ✓ ALLOWS: Viewing subjects by course                                   │
 * │ ✓ ALLOWS: Viewing subject with all assigned courses                    │
 * │ ✓ ALLOWS: Searching subjects by name or code                           │
 * │ ✗ DOES NOT: Allow modifying other teachers' subjects                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DELETION RULES                                                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ⚠ WARNING: Deleting a subject will cascade to:                         │
 * │   - All course associations (CourseSubjectCrossRef)                    │
 * │   - All attendance records (CheckIns) for this subject                 │
 * │ ⚠ IMPACT: Historical attendance data will be LOST                      │
 * │ ⚠ RECOMMENDATION: Consider soft-delete for production                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param subjectDao Data Access Object for subject database operations
 * @param courseSubjectCrossRefDao DAO for course-subject relationships
 */
class SubjectRepository(
    private val subjectDao: SubjectDao,
    private val courseSubjectCrossRefDao: CourseSubjectCrossRefDao
) {

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Retrieves all subjects in the system.
     *
     * USE CASE: Global subject directory, curriculum overview
     * ⚠ CAUTION: Returns all subjects regardless of ownership
     *
     * @return Flow of all subjects (reactive updates)
     */
    fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAllSubjects()
    }

    /**
     * Retrieves subjects owned by a specific teacher.
     *
     * BUSINESS LOGIC:
     * - Filters subjects by teacherId (creator/owner)
     * - Teachers can only modify subjects they own
     *
     * USE CASE: Teacher's subject management, creating attendance sessions
     *
     * @param teacherId The teacher's unique identifier
     * @return Flow of subjects owned by this teacher
     */
    fun getSubjectsByTeacher(teacherId: String): Flow<List<Subject>> {
        return subjectDao.getSubjectsByTeacher(teacherId)
    }

    /**
     * Retrieves subjects assigned to a specific course.
     *
     * BUSINESS LOGIC:
     * - Uses CourseSubjectCrossRef join table
     * - Subject may appear in multiple courses
     *
     * USE CASE: Course curriculum view, course-specific attendance
     *
     * @param courseId The course's unique identifier
     * @return Flow of subjects assigned to this course
     */
    fun getSubjectsByCourse(courseId: String): Flow<List<Subject>> {
        return subjectDao.getSubjectsByCourse(courseId)
    }

    /**
     * Retrieves a specific subject by ID.
     *
     * @param subjectId Subject's unique identifier
     * @return Subject if found, null otherwise
     */
    suspend fun getSubjectById(subjectId: String): Subject? {
        return subjectDao.getSubjectById(subjectId)
    }

    /**
     * Retrieves a subject by ID as a reactive Flow.
     *
     * @param subjectId Subject's unique identifier
     * @return Flow of subject (null if not found)
     */
    fun getSubjectByIdFlow(subjectId: String): Flow<Subject?> {
        return subjectDao.getSubjectByIdFlow(subjectId)
    }

    /**
     * Retrieves a subject with all assigned courses.
     *
     * USE CASE: Subject detail view showing curriculum assignments
     *
     * @param subjectId Subject's unique identifier
     * @return SubjectWithCourses containing subject and course list
     */
    suspend fun getSubjectWithCourses(subjectId: String): SubjectWithCourses? {
        return subjectDao.getSubjectWithCourses(subjectId)
    }

    /**
     * Retrieves a subject with courses as a reactive Flow.
     *
     * @param subjectId Subject's unique identifier
     * @return Flow of SubjectWithCourses
     */
    fun getSubjectWithCoursesFlow(subjectId: String): Flow<SubjectWithCourses?> {
        return subjectDao.getSubjectWithCoursesFlow(subjectId)
    }

    /**
     * Searches subjects by name or code.
     *
     * ALLOWS: Partial matching on subject name or code
     *
     * @param query Search term
     * @return Flow of matching subjects
     */
    fun searchSubjects(query: String): Flow<List<Subject>> {
        return subjectDao.searchSubjects(query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Creates a new subject.
     *
     * BUSINESS LOGIC:
     * 1. Validates required fields
     * 2. Generates unique UUID for subject
     * 3. Assigns teacher as owner
     *
     * RESTRICTIONS:
     * - Subject name cannot be blank
     * - Subject code cannot be blank
     * - Teacher ID is required (ownership)
     *
     * ALLOWS:
     * - Optional description
     * - Duplicate subject codes (different teachers)
     *
     * @param subjectName Name of the subject (required)
     * @param subjectCode Code/identifier for the subject (required)
     * @param description Optional description of the subject
     * @param teacherId Owner teacher's ID (required)
     * @return SubjectCreationResult indicating success or failure
     */
    suspend fun createSubject(
        subjectName: String,
        subjectCode: String,
        description: String = "",
        teacherId: String
    ): SubjectCreationResult {
        // Validation: Required fields
        if (subjectName.isBlank()) {
            return SubjectCreationResult.Error("Subject name is required")
        }
        if (subjectCode.isBlank()) {
            return SubjectCreationResult.Error("Subject code is required")
        }
        if (teacherId.isBlank()) {
            return SubjectCreationResult.Error("Teacher assignment is required")
        }

        // Create subject with generated ID
        val newSubject = Subject(
            subjectId = generateSubjectId(),
            subjectName = subjectName.trim(),
            subjectCode = subjectCode.trim().uppercase(),
            description = description.trim(),
            teacherId = teacherId
        )

        subjectDao.insertSubject(newSubject)
        return SubjectCreationResult.Success(newSubject)
    }

    /**
     * Creates a subject and assigns it to courses in one operation.
     *
     * BUSINESS LOGIC:
     * 1. Creates the subject
     * 2. Creates course-subject associations
     *
     * USE CASE: Creating subject for specific courses during curriculum setup
     *
     * @param subjectName Name of the subject
     * @param subjectCode Code of the subject
     * @param description Optional description
     * @param teacherId Owner teacher's ID
     * @param courseIds List of course IDs to assign to
     * @return SubjectCreationResult
     */
    suspend fun createSubjectWithCourses(
        subjectName: String,
        subjectCode: String,
        description: String = "",
        teacherId: String,
        courseIds: List<String>
    ): SubjectCreationResult {
        val result = createSubject(subjectName, subjectCode, description, teacherId)

        if (result is SubjectCreationResult.Success) {
            courseIds.forEach { courseId ->
                assignSubjectToCourse(result.subject.subjectId, courseId)
            }
        }

        return result
    }

    /**
     * Updates subject information.
     *
     * ALLOWS:
     * - Updating subject name
     * - Updating subject code
     * - Updating description
     *
     * DOES NOT:
     * - Change subject ID
     * - Modify course associations (use separate methods)
     * - Transfer ownership (teacherId should not change)
     *
     * @param subject Updated subject object
     */
    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    /**
     * Deletes a subject from the system.
     *
     * ⚠️ CASCADE EFFECTS:
     * - All CourseSubjectCrossRef records will be deleted
     * - All CheckIns (attendance records) for this subject will be deleted
     *
     * ⚠️ DATA LOSS WARNING:
     * - Historical attendance data will be permanently lost
     * - Consider archiving data before deletion
     *
     * DOES NOT AFFECT:
     * - Courses (remain with other subjects)
     * - Students (remain in system)
     * - Teacher (remains in system)
     *
     * @param subject Subject to delete
     */
    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
    }

    // ========================================================================
    // COURSE ASSIGNMENT OPERATIONS
    // ========================================================================

    /**
     * Assigns a subject to a course.
     *
     * BUSINESS LOGIC:
     * - Checks if assignment already exists
     * - Creates CourseSubjectCrossRef record
     *
     * USE CASE: Adding subject to course curriculum
     *
     * RESTRICTIONS:
     * - Cannot create duplicate assignments
     *
     * @param subjectId Subject's unique identifier
     * @param courseId Course's unique identifier
     * @return true if assigned, false if already assigned
     */
    suspend fun assignSubjectToCourse(subjectId: String, courseId: String): Boolean {
        val exists = courseSubjectCrossRefDao.exists(courseId, subjectId)
        if (exists) {
            return false // Already assigned
        }

        val crossRef = CourseSubjectCrossRef(
            courseId = courseId,
            subjectId = subjectId
        )
        courseSubjectCrossRefDao.insert(crossRef)
        return true
    }

    /**
     * Removes a subject from a course.
     *
     * BUSINESS LOGIC:
     * - Removes the CourseSubjectCrossRef record
     * - Subject record remains intact
     * - Other course assignments remain intact
     *
     * USE CASE: Curriculum restructuring, end of term cleanup
     *
     * ⚠ NOTE: Does not affect attendance records already created
     *
     * @param subjectId Subject's unique identifier
     * @param courseId Course's unique identifier
     */
    suspend fun removeSubjectFromCourse(subjectId: String, courseId: String) {
        courseSubjectCrossRefDao.deleteByIds(courseId, subjectId)
    }

    /**
     * Updates all course assignments for a subject.
     *
     * BUSINESS LOGIC:
     * 1. Removes all existing course assignments
     * 2. Creates new assignments for specified courses
     *
     * USE CASE: Bulk update of subject curriculum assignments
     *
     * @param subjectId Subject's unique identifier
     * @param courseIds List of course IDs to assign to
     */
    suspend fun updateSubjectCourses(subjectId: String, courseIds: List<String>) {
        // Remove all existing assignments
        courseSubjectCrossRefDao.deleteAllForSubject(subjectId)

        // Create new assignments
        courseIds.forEach { courseId ->
            val crossRef = CourseSubjectCrossRef(
                courseId = courseId,
                subjectId = subjectId
            )
            courseSubjectCrossRefDao.insert(crossRef)
        }
    }

    /**
     * Checks if a subject is assigned to a specific course.
     *
     * @param subjectId Subject's unique identifier
     * @param courseId Course's unique identifier
     * @return true if assigned, false otherwise
     */
    suspend fun isSubjectInCourse(subjectId: String, courseId: String): Boolean {
        return courseSubjectCrossRefDao.exists(courseId, subjectId)
    }

    /**
     * Gets the list of course IDs a subject is assigned to.
     *
     * USE CASE: Pre-populating course selection in edit forms
     *
     * @param subjectId Subject's unique identifier
     * @return List of course IDs
     */
    suspend fun getCourseIdsForSubject(subjectId: String): List<String> {
        return courseSubjectCrossRefDao.getCourseIdsForSubject(subjectId)
    }

    // ========================================================================
    // UTILITY OPERATIONS
    // ========================================================================

    /**
     * Checks if a teacher owns a specific subject.
     *
     * USE CASE: Authorization check before update/delete
     *
     * @param subjectId Subject's unique identifier
     * @param teacherId Teacher's unique identifier
     * @return true if teacher owns the subject, false otherwise
     */
    suspend fun isTeacherOwner(subjectId: String, teacherId: String): Boolean {
        val subject = subjectDao.getSubjectById(subjectId)
        return subject?.teacherId == teacherId
    }

    /**
     * Gets the count of subjects owned by a teacher.
     *
     * USE CASE: Dashboard statistics
     *
     * @param teacherId Teacher's unique identifier
     * @return Number of subjects owned
     */
    suspend fun getSubjectCountForTeacher(teacherId: String): Int {
        return subjectDao.getSubjectCountByTeacher(teacherId)
    }
    /**
     * Generates a unique masked student ID in the format STUD-XXX.
     *
     * The ID is generated by:
     * 1. Getting the max existing number from STUD-XXX formatted IDs
     * 2. Incrementing by 1
     * 3. Formatting with zero-padding (3 digits)
     *
     * @return A unique student ID like "STUD-001", "STUD-002", etc.
     */
    private suspend fun generateSubjectId(): String {
        val maxNumber = subjectDao.getMaxSubjectIdNumber() ?: 0
        var nextNumber = maxNumber + 1
        var newId = "SUBJ-%03d".format(nextNumber)

        // Handle potential conflicts (race condition safety)
        while (subjectDao.subjectIdExists(newId)) {
            nextNumber++
            newId = "SUBJ-%03d".format(nextNumber)
        }

        return newId
    }
}

/**
 * Sealed class representing subject creation operation results.
 */
sealed class SubjectCreationResult {
    /**
     * Subject created successfully.
     * @param subject The newly created subject
     */
    data class Success(val subject: Subject) : SubjectCreationResult()

    /**
     * Subject creation failed.
     * @param message Human-readable error description
     */
    data class Error(val message: String) : SubjectCreationResult()
}


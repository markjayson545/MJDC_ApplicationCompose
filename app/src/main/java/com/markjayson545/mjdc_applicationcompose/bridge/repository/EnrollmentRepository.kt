package com.markjayson545.mjdc_applicationcompose.bridge.repository

import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.StudentSubjectCrossRefDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentSubjectCrossRef
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithSubjects
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.SubjectWithEnrolledStudents
import kotlinx.coroutines.flow.Flow

/**
 * ============================================================================
 * ENROLLMENT REPOSITORY
 * ============================================================================
 *
 * This repository manages all business logic related to student-subject
 * enrollments in the attendance system. It handles the many-to-many
 * relationship between students and subjects.
 *
 * KEY FEATURES:
 * - Student enrollment in subjects
 * - Bulk enrollment operations
 * - Enrollment queries and filtering
 * - Enrollment count tracking
 *
 * ENTITY RELATIONSHIPS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      ENROLLMENT RELATIONSHIPS                           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │   Student ←──[N:M]──→ Subject (via StudentSubjectCrossRef)             │
 * │                                                                         │
 * │   USE CASES:                                                            │
 * │   - A student is enrolled in multiple subjects                          │
 * │   - A subject has multiple students enrolled                            │
 * │   - Attendance filtering shows only enrolled students                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * USAGE PATTERNS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ FROM MANAGE STUDENTS SCREEN:                                            │
 * │ - enrollStudentInSubjects(studentId, subjectIds) - bulk enroll          │
 * │ - getEnrolledSubjectIds(studentId) - get current enrollments            │
 * │ - updateStudentEnrollments(studentId, newSubjectIds) - sync             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ FROM MANAGE SUBJECTS SCREEN:                                            │
 * │ - enrollStudentsInSubject(subjectId, studentIds) - bulk enroll          │
 * │ - getEnrolledStudentIds(subjectId) - get current enrollments            │
 * │ - updateSubjectEnrollments(subjectId, newStudentIds) - sync             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ FROM ATTENDANCE SCREEN:                                                 │
 * │ - getEnrolledStudents(subjectId) - filter student list                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param studentSubjectCrossRefDao DAO for enrollment database operations
 */
class EnrollmentRepository(
    private val studentSubjectCrossRefDao: StudentSubjectCrossRefDao
) {

    // ========================================================================
    // READ OPERATIONS - FLOW (Reactive)
    // ========================================================================

    /**
     * Get a student with their enrolled subjects as a reactive Flow.
     *
     * @param studentId The student ID
     * @return Flow of StudentWithSubjects or null
     */
    fun getStudentWithSubjects(studentId: String): Flow<StudentWithSubjects?> {
        return studentSubjectCrossRefDao.getStudentWithSubjects(studentId)
    }

    /**
     * Get a subject with its enrolled students as a reactive Flow.
     *
     * @param subjectId The subject ID
     * @return Flow of SubjectWithEnrolledStudents or null
     */
    fun getSubjectWithEnrolledStudents(subjectId: String): Flow<SubjectWithEnrolledStudents?> {
        return studentSubjectCrossRefDao.getSubjectWithEnrolledStudents(subjectId)
    }

    /**
     * Get enrollment count for a student as a reactive Flow.
     *
     * @param studentId The student ID
     * @return Flow of enrollment count
     */
    fun getEnrollmentCountByStudentFlow(studentId: String): Flow<Int> {
        return studentSubjectCrossRefDao.getEnrollmentCountByStudentFlow(studentId)
    }

    /**
     * Get enrollment count for a subject as a reactive Flow.
     *
     * @param subjectId The subject ID
     * @return Flow of enrollment count
     */
    fun getEnrollmentCountBySubjectFlow(subjectId: String): Flow<Int> {
        return studentSubjectCrossRefDao.getEnrollmentCountBySubjectFlow(subjectId)
    }

    // ========================================================================
    // READ OPERATIONS - SUSPEND (One-time)
    // ========================================================================

    /**
     * Get all subject IDs that a student is enrolled in.
     *
     * @param studentId The student ID
     * @return List of subject IDs
     */
    suspend fun getEnrolledSubjectIds(studentId: String): List<String> {
        return studentSubjectCrossRefDao.getSubjectIdsByStudent(studentId)
    }

    /**
     * Get all student IDs enrolled in a subject.
     *
     * @param subjectId The subject ID
     * @return List of student IDs
     */
    suspend fun getEnrolledStudentIds(subjectId: String): List<String> {
        return studentSubjectCrossRefDao.getStudentIdsBySubject(subjectId)
    }

    /**
     * Check if a student is enrolled in a subject.
     *
     * @param studentId The student ID
     * @param subjectId The subject ID
     * @return true if enrolled, false otherwise
     */
    suspend fun isEnrolled(studentId: String, subjectId: String): Boolean {
        return studentSubjectCrossRefDao.exists(studentId, subjectId)
    }

    /**
     * Get enrollment count for a student.
     *
     * @param studentId The student ID
     * @return Number of subjects the student is enrolled in
     */
    suspend fun getEnrollmentCountByStudent(studentId: String): Int {
        return studentSubjectCrossRefDao.getEnrollmentCountByStudent(studentId)
    }

    /**
     * Get enrollment count for a subject.
     *
     * @param subjectId The subject ID
     * @return Number of students enrolled in the subject
     */
    suspend fun getEnrollmentCountBySubject(subjectId: String): Int {
        return studentSubjectCrossRefDao.getEnrollmentCountBySubject(subjectId)
    }

    /**
     * Get enrollment counts for multiple students at once.
     * Returns a map of studentId to enrollment count.
     *
     * @param studentIds List of student IDs
     * @return Map of studentId to enrollment count
     */
    suspend fun getEnrollmentCountsForStudents(studentIds: List<String>): Map<String, Int> {
        if (studentIds.isEmpty()) return emptyMap()
        val enrollments = studentSubjectCrossRefDao.getByStudentIds(studentIds)
        return enrollments.groupBy { it.studentId }.mapValues { it.value.size }
    }

    /**
     * Get enrollment counts for multiple subjects at once.
     * Returns a map of subjectId to enrollment count.
     *
     * @param subjectIds List of subject IDs
     * @return Map of subjectId to enrollment count
     */
    suspend fun getEnrollmentCountsForSubjects(subjectIds: List<String>): Map<String, Int> {
        if (subjectIds.isEmpty()) return emptyMap()
        val enrollments = studentSubjectCrossRefDao.getBySubjectIds(subjectIds)
        return enrollments.groupBy { it.subjectId }.mapValues { it.value.size }
    }

    // ========================================================================
    // SINGLE ENROLLMENT OPERATIONS
    // ========================================================================

    /**
     * Enroll a student in a single subject.
     *
     * @param studentId The student ID
     * @param subjectId The subject ID
     */
    suspend fun enrollStudent(studentId: String, subjectId: String) {
        studentSubjectCrossRefDao.insert(
            StudentSubjectCrossRef(studentId, subjectId)
        )
    }

    /**
     * Unenroll a student from a single subject.
     *
     * @param studentId The student ID
     * @param subjectId The subject ID
     */
    suspend fun unenrollStudent(studentId: String, subjectId: String) {
        studentSubjectCrossRefDao.deleteByIds(studentId, subjectId)
    }

    // ========================================================================
    // BULK ENROLLMENT OPERATIONS - FROM STUDENT SIDE
    // ========================================================================

    /**
     * Enroll a student in multiple subjects at once.
     *
     * @param studentId The student ID
     * @param subjectIds List of subject IDs to enroll in
     */
    suspend fun enrollStudentInSubjects(studentId: String, subjectIds: List<String>) {
        if (subjectIds.isEmpty()) return
        val crossRefs = subjectIds.map { StudentSubjectCrossRef(studentId, it) }
        studentSubjectCrossRefDao.insertAll(crossRefs)
    }

    /**
     * Update a student's enrollments to match the given subject list.
     * Removes enrollments not in the list, adds new ones.
     *
     * @param studentId The student ID
     * @param newSubjectIds The new list of subject IDs the student should be enrolled in
     */
    suspend fun updateStudentEnrollments(studentId: String, newSubjectIds: List<String>) {
        val currentSubjectIds = getEnrolledSubjectIds(studentId).toSet()
        val newSubjectIdsSet = newSubjectIds.toSet()

        // Remove enrollments that are no longer needed
        val toRemove = currentSubjectIds - newSubjectIdsSet
        toRemove.forEach { subjectId ->
            studentSubjectCrossRefDao.deleteByIds(studentId, subjectId)
        }

        // Add new enrollments
        val toAdd = newSubjectIdsSet - currentSubjectIds
        if (toAdd.isNotEmpty()) {
            val crossRefs = toAdd.map { StudentSubjectCrossRef(studentId, it) }
            studentSubjectCrossRefDao.insertAll(crossRefs)
        }
    }

    /**
     * Remove all enrollments for a student.
     *
     * @param studentId The student ID
     */
    suspend fun unenrollStudentFromAll(studentId: String) {
        studentSubjectCrossRefDao.deleteAllByStudent(studentId)
    }

    // ========================================================================
    // BULK ENROLLMENT OPERATIONS - FROM SUBJECT SIDE
    // ========================================================================

    /**
     * Enroll multiple students in a subject at once.
     *
     * @param subjectId The subject ID
     * @param studentIds List of student IDs to enroll
     */
    suspend fun enrollStudentsInSubject(subjectId: String, studentIds: List<String>) {
        if (studentIds.isEmpty()) return
        val crossRefs = studentIds.map { StudentSubjectCrossRef(it, subjectId) }
        studentSubjectCrossRefDao.insertAll(crossRefs)
    }

    /**
     * Update a subject's enrollments to match the given student list.
     * Removes students not in the list, adds new ones.
     *
     * @param subjectId The subject ID
     * @param newStudentIds The new list of student IDs that should be enrolled
     */
    suspend fun updateSubjectEnrollments(subjectId: String, newStudentIds: List<String>) {
        val currentStudentIds = getEnrolledStudentIds(subjectId).toSet()
        val newStudentIdsSet = newStudentIds.toSet()

        // Remove students that are no longer enrolled
        val toRemove = currentStudentIds - newStudentIdsSet
        toRemove.forEach { studentId ->
            studentSubjectCrossRefDao.deleteByIds(studentId, subjectId)
        }

        // Add new students
        val toAdd = newStudentIdsSet - currentStudentIds
        if (toAdd.isNotEmpty()) {
            val crossRefs = toAdd.map { StudentSubjectCrossRef(it, subjectId) }
            studentSubjectCrossRefDao.insertAll(crossRefs)
        }
    }

    /**
     * Remove all enrollments for a subject.
     *
     * @param subjectId The subject ID
     */
    suspend fun unenrollAllFromSubject(subjectId: String) {
        studentSubjectCrossRefDao.deleteAllBySubject(subjectId)
    }
}

/**
 * Result wrapper for enrollment operations.
 */
sealed class EnrollmentResult {
    data object Success : EnrollmentResult()
    data class Error(val message: String) : EnrollmentResult()
}


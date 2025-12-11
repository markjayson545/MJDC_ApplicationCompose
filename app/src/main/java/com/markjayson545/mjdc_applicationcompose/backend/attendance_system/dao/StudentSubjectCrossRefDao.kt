package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentSubjectCrossRef
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithSubjects
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.SubjectWithEnrolledStudents
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Student-Subject enrollment relationships.
 *
 * OPERATIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ CRUD Operations                                                         │
 * │ - insert/insertAll: Enroll student(s) in subject(s)                    │
 * │ - delete/deleteByIds: Unenroll student from subject                    │
 * │ - deleteAllByStudent: Remove all enrollments for a student             │
 * │ - deleteAllBySubject: Remove all enrollments for a subject             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Query Operations                                                        │
 * │ - getByStudent: Get all enrollments for a student (Flow)               │
 * │ - getBySubject: Get all enrollments for a subject (Flow)               │
 * │ - getStudentIdsBySubject: Get enrolled student IDs                     │
 * │ - getSubjectIdsByStudent: Get enrolled subject IDs                     │
 * │ - exists: Check if enrollment exists                                    │
 * │ - getEnrollmentCount: Count subjects for a student                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@Dao
interface StudentSubjectCrossRefDao {

    // ========================================================================
    // INSERT OPERATIONS
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: StudentSubjectCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<StudentSubjectCrossRef>)

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    @Delete
    suspend fun delete(crossRef: StudentSubjectCrossRef)

    @Query("DELETE FROM student_subject_cross_ref WHERE studentId = :studentId AND subjectId = :subjectId")
    suspend fun deleteByIds(studentId: String, subjectId: String)

    @Query("DELETE FROM student_subject_cross_ref WHERE studentId = :studentId")
    suspend fun deleteAllByStudent(studentId: String)

    @Query("DELETE FROM student_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun deleteAllBySubject(subjectId: String)

    // ========================================================================
    // FLOW QUERIES (Reactive)
    // ========================================================================

    @Query("SELECT * FROM student_subject_cross_ref WHERE studentId = :studentId")
    fun getByStudent(studentId: String): Flow<List<StudentSubjectCrossRef>>

    @Query("SELECT * FROM student_subject_cross_ref WHERE subjectId = :subjectId")
    fun getBySubject(subjectId: String): Flow<List<StudentSubjectCrossRef>>

    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    fun getStudentWithSubjects(studentId: String): Flow<StudentWithSubjects?>

    @Transaction
    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    fun getSubjectWithEnrolledStudents(subjectId: String): Flow<SubjectWithEnrolledStudents?>

    // ========================================================================
    // SUSPEND QUERIES (One-time)
    // ========================================================================

    @Query("SELECT * FROM student_subject_cross_ref WHERE studentId = :studentId")
    suspend fun getByStudentList(studentId: String): List<StudentSubjectCrossRef>

    @Query("SELECT * FROM student_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun getBySubjectList(subjectId: String): List<StudentSubjectCrossRef>

    @Query("SELECT studentId FROM student_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun getStudentIdsBySubject(subjectId: String): List<String>

    @Query("SELECT subjectId FROM student_subject_cross_ref WHERE studentId = :studentId")
    suspend fun getSubjectIdsByStudent(studentId: String): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM student_subject_cross_ref WHERE studentId = :studentId AND subjectId = :subjectId)")
    suspend fun exists(studentId: String, subjectId: String): Boolean

    // ========================================================================
    // COUNT QUERIES
    // ========================================================================

    @Query("SELECT COUNT(*) FROM student_subject_cross_ref WHERE studentId = :studentId")
    suspend fun getEnrollmentCountByStudent(studentId: String): Int

    @Query("SELECT COUNT(*) FROM student_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun getEnrollmentCountBySubject(subjectId: String): Int

    @Query("SELECT COUNT(*) FROM student_subject_cross_ref WHERE studentId = :studentId")
    fun getEnrollmentCountByStudentFlow(studentId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM student_subject_cross_ref WHERE subjectId = :subjectId")
    fun getEnrollmentCountBySubjectFlow(subjectId: String): Flow<Int>

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Get all enrollments for multiple students at once.
     * Useful for displaying enrollment counts in lists.
     */
    @Query("SELECT * FROM student_subject_cross_ref WHERE studentId IN (:studentIds)")
    suspend fun getByStudentIds(studentIds: List<String>): List<StudentSubjectCrossRef>

    /**
     * Get all enrollments for multiple subjects at once.
     */
    @Query("SELECT * FROM student_subject_cross_ref WHERE subjectId IN (:subjectIds)")
    suspend fun getBySubjectIds(subjectIds: List<String>): List<StudentSubjectCrossRef>
}


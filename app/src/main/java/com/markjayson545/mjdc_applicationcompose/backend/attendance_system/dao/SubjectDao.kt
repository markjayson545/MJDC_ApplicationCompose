package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.SubjectWithAttendance
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.SubjectWithCourses
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE subjectId = :subjectId")
    suspend fun deleteSubjectById(subjectId: String)

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    suspend fun getSubjectById(subjectId: String): Subject?

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    fun getSubjectByIdFlow(subjectId: String): Flow<Subject?>

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsList(): List<Subject>

    // Query by teacher
    @Query("SELECT * FROM subjects WHERE teacherId = :teacherId")
    fun getSubjectsByTeacher(teacherId: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE teacherId = :teacherId")
    suspend fun getSubjectsByTeacherList(teacherId: String): List<Subject>

    // Query subjects by course (via cross-ref)
    @Query("""
        SELECT s.* FROM subjects s
        INNER JOIN course_subject_cross_ref csc ON s.subjectId = csc.subjectId
        WHERE csc.courseId = :courseId
    """)
    fun getSubjectsByCourse(courseId: String): Flow<List<Subject>>

    @Query("""
        SELECT s.* FROM subjects s
        INNER JOIN course_subject_cross_ref csc ON s.subjectId = csc.subjectId
        WHERE csc.courseId = :courseId
    """)
    suspend fun getSubjectsByCourseList(courseId: String): List<Subject>

    // Relation queries
    @Transaction
    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    suspend fun getSubjectWithCourses(subjectId: String): SubjectWithCourses?

    @Transaction
    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    fun getSubjectWithCoursesFlow(subjectId: String): Flow<SubjectWithCourses?>

    @Transaction
    @Query("SELECT * FROM subjects WHERE teacherId = :teacherId")
    fun getSubjectsWithCoursesByTeacher(teacherId: String): Flow<List<SubjectWithCourses>>

    @Transaction
    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    suspend fun getSubjectWithAttendance(subjectId: String): SubjectWithAttendance?

    @Transaction
    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId")
    fun getSubjectWithAttendanceFlow(subjectId: String): Flow<SubjectWithAttendance?>

    // Search
    @Query("""
        SELECT * FROM subjects 
        WHERE subjectName LIKE '%' || :query || '%' 
        OR subjectCode LIKE '%' || :query || '%'
    """)
    fun searchSubjects(query: String): Flow<List<Subject>>

    // Search by teacher
    @Query("""
        SELECT * FROM subjects 
        WHERE teacherId = :teacherId
        AND (subjectName LIKE '%' || :query || '%' 
        OR subjectCode LIKE '%' || :query || '%')
    """)
    fun searchSubjectsByTeacher(teacherId: String, query: String): Flow<List<Subject>>

    // Count
    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Query("SELECT COUNT(*) FROM subjects WHERE teacherId = :teacherId")
    suspend fun getSubjectCountByTeacher(teacherId: String): Int

    // Check if subject code exists
    @Query("SELECT EXISTS(SELECT 1 FROM subjects WHERE subjectCode = :subjectCode)")
    suspend fun isSubjectCodeExists(subjectCode: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM subjects WHERE subjectCode = :subjectCode AND subjectId != :excludeSubjectId)")
    suspend fun isSubjectCodeExistsExcluding(subjectCode: String, excludeSubjectId: String): Boolean

    @Query("""
        SELECT MAX(CAST(SUBSTR(subjectId, 6) AS INTEGER))
        FROM subjects 
        WHERE subjectId LIKE 'SUBJ-%'
    """)
    suspend fun getMaxSubjectIdNumber(): Int?

    @Query("SELECT EXISTS(SELECT 1 FROM subjects WHERE subjectId = :subjectId)")
    suspend fun subjectIdExists(subjectId: String): Boolean
}

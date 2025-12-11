package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CourseWithSubjects
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses WHERE courseId = :courseId")
    suspend fun deleteCourseById(courseId: String)

    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    suspend fun getCourseById(courseId: String): Course?

    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    fun getCourseByIdFlow(courseId: String): Flow<Course?>

    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses")
    suspend fun getAllCoursesList(): List<Course>

    // Query by teacher
    @Query("SELECT * FROM courses WHERE teacherId = :teacherId")
    fun getCoursesByTeacher(teacherId: String): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE teacherId = :teacherId")
    suspend fun getCoursesByTeacherList(teacherId: String): List<Course>

    // Relation queries
    @Transaction
    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    suspend fun getCourseWithSubjects(courseId: String): CourseWithSubjects?

    @Transaction
    @Query("SELECT * FROM courses WHERE courseId = :courseId")
    fun getCourseWithSubjectsFlow(courseId: String): Flow<CourseWithSubjects?>

    @Transaction
    @Query("SELECT * FROM courses WHERE teacherId = :teacherId")
    fun getCoursesWithSubjectsByTeacher(teacherId: String): Flow<List<CourseWithSubjects>>

    // Search
    @Query("""
        SELECT * FROM courses 
        WHERE courseName LIKE '%' || :query || '%' 
        OR courseCode LIKE '%' || :query || '%'
    """)
    fun searchCourses(query: String): Flow<List<Course>>

    // Search by teacher
    @Query("""
        SELECT * FROM courses 
        WHERE teacherId = :teacherId
        AND (courseName LIKE '%' || :query || '%' 
        OR courseCode LIKE '%' || :query || '%')
    """)
    fun searchCoursesByTeacher(teacherId: String, query: String): Flow<List<Course>>

    // Count
    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Query("SELECT COUNT(*) FROM courses WHERE teacherId = :teacherId")
    suspend fun getCourseCountByTeacher(teacherId: String): Int

    // Check if course code exists
    @Query("SELECT EXISTS(SELECT 1 FROM courses WHERE courseCode = :courseCode)")
    suspend fun isCourseCodeExists(courseCode: String): Boolean

    @Query("""
        SELECT MAX(CAST(SUBSTR(courseId, 6) AS INTEGER)) 
        FROM courses 
        WHERE courseId LIKE 'COUR-%'
    """)
    suspend fun getMaxCourseIdNumber(): Int?

    @Query("SELECT EXISTS(SELECT 1 FROM courses WHERE courseId = :courseId)")
    suspend fun courseIdExists(courseId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM courses WHERE courseCode = :courseCode AND courseId != :excludeCourseId)")
    suspend fun isCourseCodeExistsExcluding(courseCode: String, excludeCourseId: String): Boolean
}

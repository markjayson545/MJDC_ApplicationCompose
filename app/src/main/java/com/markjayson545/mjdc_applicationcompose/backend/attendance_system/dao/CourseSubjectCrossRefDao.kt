package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CourseSubjectCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseSubjectCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: CourseSubjectCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<CourseSubjectCrossRef>)

    @Delete
    suspend fun delete(crossRef: CourseSubjectCrossRef)

    @Query("DELETE FROM course_subject_cross_ref WHERE courseId = :courseId AND subjectId = :subjectId")
    suspend fun deleteByIds(courseId: String, subjectId: String)

    @Query("DELETE FROM course_subject_cross_ref WHERE courseId = :courseId")
    suspend fun deleteAllByCourse(courseId: String)

    @Query("DELETE FROM course_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun deleteAllBySubject(subjectId: String)

    @Query("DELETE FROM course_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun deleteAllForSubject(subjectId: String)

    @Query("SELECT * FROM course_subject_cross_ref WHERE courseId = :courseId")
    fun getByCourse(courseId: String): Flow<List<CourseSubjectCrossRef>>

    @Query("SELECT * FROM course_subject_cross_ref WHERE courseId = :courseId")
    suspend fun getByCourseList(courseId: String): List<CourseSubjectCrossRef>

    @Query("SELECT * FROM course_subject_cross_ref WHERE subjectId = :subjectId")
    fun getBySubject(subjectId: String): Flow<List<CourseSubjectCrossRef>>

    @Query("SELECT * FROM course_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun getBySubjectList(subjectId: String): List<CourseSubjectCrossRef>

    @Query("SELECT EXISTS(SELECT 1 FROM course_subject_cross_ref WHERE courseId = :courseId AND subjectId = :subjectId)")
    suspend fun exists(courseId: String, subjectId: String): Boolean

    @Query("SELECT COUNT(*) FROM course_subject_cross_ref WHERE courseId = :courseId")
    suspend fun getSubjectCountByCourse(courseId: String): Int

    @Query("SELECT COUNT(*) FROM course_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun getCourseCountBySubject(subjectId: String): Int

    @Query("SELECT courseId FROM course_subject_cross_ref WHERE subjectId = :subjectId")
    suspend fun getCourseIdsForSubject(subjectId: String): List<String>
}


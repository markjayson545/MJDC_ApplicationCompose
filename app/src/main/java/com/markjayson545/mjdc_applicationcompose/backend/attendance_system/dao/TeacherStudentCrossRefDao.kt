package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherStudentCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherStudentCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: TeacherStudentCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<TeacherStudentCrossRef>)

    @Delete
    suspend fun delete(crossRef: TeacherStudentCrossRef)

    @Query("DELETE FROM teacher_student_cross_ref WHERE teacherId = :teacherId AND studentId = :studentId")
    suspend fun deleteByIds(teacherId: String, studentId: String)

    @Query("DELETE FROM teacher_student_cross_ref WHERE teacherId = :teacherId")
    suspend fun deleteAllByTeacher(teacherId: String)

    @Query("DELETE FROM teacher_student_cross_ref WHERE studentId = :studentId")
    suspend fun deleteAllByStudent(studentId: String)

    @Query("SELECT * FROM teacher_student_cross_ref WHERE teacherId = :teacherId")
    fun getByTeacher(teacherId: String): Flow<List<TeacherStudentCrossRef>>

    @Query("SELECT * FROM teacher_student_cross_ref WHERE teacherId = :teacherId")
    suspend fun getByTeacherList(teacherId: String): List<TeacherStudentCrossRef>

    @Query("SELECT * FROM teacher_student_cross_ref WHERE studentId = :studentId")
    fun getByStudent(studentId: String): Flow<List<TeacherStudentCrossRef>>

    @Query("SELECT * FROM teacher_student_cross_ref WHERE studentId = :studentId")
    suspend fun getByStudentList(studentId: String): List<TeacherStudentCrossRef>

    @Query("SELECT EXISTS(SELECT 1 FROM teacher_student_cross_ref WHERE teacherId = :teacherId AND studentId = :studentId)")
    suspend fun exists(teacherId: String, studentId: String): Boolean

    @Query("SELECT COUNT(*) FROM teacher_student_cross_ref WHERE teacherId = :teacherId")
    suspend fun getStudentCountByTeacher(teacherId: String): Int

    @Query("SELECT COUNT(*) FROM teacher_student_cross_ref WHERE teacherId = :teacherId")
    suspend fun getStudentCountForTeacher(teacherId: String): Int

    @Query("SELECT COUNT(*) FROM teacher_student_cross_ref WHERE studentId = :studentId")
    suspend fun getTeacherCountByStudent(studentId: String): Int
}


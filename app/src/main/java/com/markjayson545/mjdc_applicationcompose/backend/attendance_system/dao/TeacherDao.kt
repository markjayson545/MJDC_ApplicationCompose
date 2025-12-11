package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Teacher
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherWithAllData
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherWithStudents
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId")
    suspend fun getTeacherById(teacherId: String): Teacher?

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId")
    fun getTeacherByIdFlow(teacherId: String): Flow<Teacher?>

    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers")
    suspend fun getAllTeachersList(): List<Teacher>

    // Authentication queries
    @Query("SELECT * FROM teachers WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Teacher?

    @Query("SELECT * FROM teachers WHERE email = :email LIMIT 1")
    suspend fun getTeacherByEmail(email: String): Teacher?

    @Query("SELECT EXISTS(SELECT 1 FROM teachers WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean

    // Relation queries
    @Transaction
    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId")
    suspend fun getTeacherWithStudents(teacherId: String): TeacherWithStudents?

    @Transaction
    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId")
    fun getTeacherWithStudentsFlow(teacherId: String): Flow<TeacherWithStudents?>

    @Transaction
    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId")
    suspend fun getTeacherWithAllData(teacherId: String): TeacherWithAllData?

    // Search
    @Query("SELECT * FROM teachers WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%'")
    fun searchTeachers(query: String): Flow<List<Teacher>>

    // Get max masked ID number for generating next teacher ID (e.g., TEACH-001)
    @Query("""
        SELECT MAX(CAST(SUBSTR(teacherId, 7) AS INTEGER)) 
        FROM teachers 
        WHERE teacherId LIKE 'TEACH-%'
    """)
    suspend fun getMaxTeacherIdNumber(): Int?

    // Check if a specific teacher ID exists
    @Query("SELECT EXISTS(SELECT 1 FROM teachers WHERE teacherId = :teacherId)")
    suspend fun teacherIdExists(teacherId: String): Boolean
}


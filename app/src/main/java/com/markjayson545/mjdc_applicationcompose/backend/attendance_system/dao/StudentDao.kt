package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithAttendance
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithTeachers
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudentById(studentId: String)

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentById(studentId: String): Student?

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    fun getStudentByIdFlow(studentId: String): Flow<Student?>

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsList(): List<Student>

    // Query by course
    @Query("SELECT * FROM students WHERE courseId = :courseId")
    fun getStudentsByCourse(courseId: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE courseId = :courseId")
    suspend fun getStudentsByCourseList(courseId: String): List<Student>

    // Query students by teacher (via cross-ref)
    @Query("""
        SELECT s.* FROM students s
        INNER JOIN teacher_student_cross_ref tsc ON s.studentId = tsc.studentId
        WHERE tsc.teacherId = :teacherId
    """)
    fun getStudentsByTeacher(teacherId: String): Flow<List<Student>>

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN teacher_student_cross_ref tsc ON s.studentId = tsc.studentId
        WHERE tsc.teacherId = :teacherId
    """)
    suspend fun getStudentsByTeacherList(teacherId: String): List<Student>

    // Relation queries
    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentWithTeachers(studentId: String): StudentWithTeachers?

    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    fun getStudentWithTeachersFlow(studentId: String): Flow<StudentWithTeachers?>

    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    suspend fun getStudentWithAttendance(studentId: String): StudentWithAttendance?

    @Transaction
    @Query("SELECT * FROM students WHERE studentId = :studentId")
    fun getStudentWithAttendanceFlow(studentId: String): Flow<StudentWithAttendance?>

    // Search
    @Query("""
        SELECT * FROM students 
        WHERE firstName LIKE '%' || :query || '%' 
        OR lastName LIKE '%' || :query || '%' 
        OR studentId LIKE '%' || :query || '%'
    """)
    fun searchStudents(query: String): Flow<List<Student>>

    // Search students by teacher
    @Query("""
        SELECT s.* FROM students s
        INNER JOIN teacher_student_cross_ref tsc ON s.studentId = tsc.studentId
        WHERE tsc.teacherId = :teacherId
        AND (s.firstName LIKE '%' || :query || '%' 
        OR s.lastName LIKE '%' || :query || '%' 
        OR s.studentId LIKE '%' || :query || '%')
    """)
    fun searchStudentsByTeacher(teacherId: String, query: String): Flow<List<Student>>

    // Count
    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Query("SELECT COUNT(*) FROM students WHERE courseId = :courseId")
    suspend fun getStudentCountByCourse(courseId: String): Int

    @Query("""
        SELECT COUNT(*) FROM students s
        INNER JOIN teacher_student_cross_ref tsc ON s.studentId = tsc.studentId
        WHERE tsc.teacherId = :teacherId
    """)
    suspend fun getStudentCountByTeacher(teacherId: String): Int

    // Get max masked ID number for generating next student ID (e.g., STUD-001)
    @Query("""
        SELECT MAX(CAST(SUBSTR(studentId, 6) AS INTEGER)) 
        FROM students 
        WHERE studentId LIKE 'STUD-%'
    """)
    suspend fun getMaxStudentIdNumber(): Int?

    // Check if a specific student ID exists
    @Query("SELECT EXISTS(SELECT 1 FROM students WHERE studentId = :studentId)")
    suspend fun studentIdExists(studentId: String): Boolean
}
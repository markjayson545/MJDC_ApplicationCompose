package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CheckIns
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInsDao {

    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIns)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIns(checkIns: List<CheckIns>)

    @Update
    suspend fun updateCheckIn(checkIn: CheckIns)

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckIns)

    @Query("DELETE FROM check_ins WHERE checkInId = :checkInId")
    suspend fun deleteCheckInById(checkInId: String)

    @Query("SELECT * FROM check_ins WHERE checkInId = :checkInId")
    suspend fun getCheckInById(checkInId: String): CheckIns?

    @Query("SELECT * FROM check_ins")
    fun getAllCheckIns(): Flow<List<CheckIns>>

    @Query("SELECT * FROM check_ins")
    suspend fun getAllCheckInsList(): List<CheckIns>

    // Query by student
    @Query("SELECT * FROM check_ins WHERE studentId = :studentId ORDER BY checkInDate DESC, checkInTime DESC")
    fun getCheckInsByStudent(studentId: String): Flow<List<CheckIns>>

    @Query("SELECT * FROM check_ins WHERE studentId = :studentId ORDER BY checkInDate DESC, checkInTime DESC")
    suspend fun getCheckInsByStudentList(studentId: String): List<CheckIns>

    // Query by subject
    @Query("SELECT * FROM check_ins WHERE subjectId = :subjectId ORDER BY checkInDate DESC, checkInTime DESC")
    fun getCheckInsBySubject(subjectId: String): Flow<List<CheckIns>>

    @Query("SELECT * FROM check_ins WHERE subjectId = :subjectId ORDER BY checkInDate DESC, checkInTime DESC")
    suspend fun getCheckInsBySubjectList(subjectId: String): List<CheckIns>

    // Query by teacher
    @Query("SELECT * FROM check_ins WHERE teacherId = :teacherId ORDER BY checkInDate DESC, checkInTime DESC")
    fun getCheckInsByTeacher(teacherId: String): Flow<List<CheckIns>>

    @Query("SELECT * FROM check_ins WHERE teacherId = :teacherId ORDER BY checkInDate DESC, checkInTime DESC")
    suspend fun getCheckInsByTeacherList(teacherId: String): List<CheckIns>

    // Query by date
    @Query("SELECT * FROM check_ins WHERE checkInDate = :date ORDER BY checkInTime DESC")
    fun getCheckInsByDate(date: String): Flow<List<CheckIns>>

    @Query("SELECT * FROM check_ins WHERE checkInDate = :date AND teacherId = :teacherId ORDER BY checkInTime DESC")
    fun getCheckInsByDateAndTeacher(date: String, teacherId: String): Flow<List<CheckIns>>

    // Query by subject and date
    @Query("SELECT * FROM check_ins WHERE subjectId = :subjectId AND checkInDate = :date ORDER BY checkInTime DESC")
    fun getCheckInsBySubjectAndDate(subjectId: String, date: String): Flow<List<CheckIns>>

    @Query("SELECT * FROM check_ins WHERE subjectId = :subjectId AND checkInDate = :date ORDER BY checkInTime DESC")
    suspend fun getCheckInsBySubjectAndDateList(subjectId: String, date: String): List<CheckIns>

    // Query by student and subject
    @Query("SELECT * FROM check_ins WHERE studentId = :studentId AND subjectId = :subjectId ORDER BY checkInDate DESC, checkInTime DESC")
    fun getCheckInsByStudentAndSubject(studentId: String, subjectId: String): Flow<List<CheckIns>>

    // Check if student already checked in for subject on date
    @Query("SELECT EXISTS(SELECT 1 FROM check_ins WHERE studentId = :studentId AND subjectId = :subjectId AND checkInDate = :date)")
    suspend fun hasCheckedIn(studentId: String, subjectId: String, date: String): Boolean

    // Count queries
    @Query("SELECT COUNT(*) FROM check_ins WHERE teacherId = :teacherId")
    suspend fun getCheckInCountByTeacher(teacherId: String): Int

    @Query("SELECT COUNT(*) FROM check_ins WHERE teacherId = :teacherId AND checkInDate = :date")
    suspend fun getCheckInCountByTeacherAndDate(teacherId: String, date: String): Int

    @Query("SELECT COUNT(*) FROM check_ins WHERE subjectId = :subjectId AND checkInDate = :date")
    suspend fun getCheckInCountBySubjectAndDate(subjectId: String, date: String): Int

    // Status based queries
    @Query("SELECT COUNT(*) FROM check_ins WHERE teacherId = :teacherId AND status = :status")
    suspend fun getCheckInCountByTeacherAndStatus(teacherId: String, status: AttendanceStatus): Int

    @Query("SELECT * FROM check_ins WHERE teacherId = :teacherId AND status = :status ORDER BY checkInDate DESC, checkInTime DESC")
    fun getCheckInsByTeacherAndStatus(teacherId: String, status: AttendanceStatus): Flow<List<CheckIns>>

    // Delete all check-ins for a specific date and subject
    @Query("DELETE FROM check_ins WHERE subjectId = :subjectId AND checkInDate = :date")
    suspend fun deleteCheckInsBySubjectAndDate(subjectId: String, date: String)
}

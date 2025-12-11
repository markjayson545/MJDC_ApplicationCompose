package com.markjayson545.mjdc_applicationcompose.bridge.repository

import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.CheckInsDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CheckIns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ============================================================================
 * ATTENDANCE REPOSITORY
 * ============================================================================
 *
 * This repository manages all business logic related to attendance tracking
 * in the attendance system. This is the CORE functionality of the entire
 * application, handling check-ins, status tracking, and reporting.
 *
 * KEY FEATURES:
 * - Recording student attendance
 * - Multiple attendance status types
 * - Bulk attendance recording
 * - Historical attendance tracking
 * - Statistical reporting
 *
 * ATTENDANCE STATUS TYPES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ STATUS          │ DESCRIPTION                                           │
 * ├─────────────────┼──────────────────────────────────────────────────────-┤
 * │ PRESENT         │ Student attended class on time                        │
 * │ ABSENT          │ Student did not attend class                          │
 * │ LATE            │ Student attended but arrived late                     │
 * │ EXCUSED         │ Student absent with valid excuse (medical, etc.)      │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ENTITY RELATIONSHIPS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      CHECK-IN RELATIONSHIPS                             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │   Student ←──[1:N]──→ CheckIns (attendance records)                     │
 * │   Subject ←──[1:N]──→ CheckIns (class attended)                         │
 * │   Teacher ←──[1:N]──→ CheckIns (who recorded)                           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * BUSINESS RULES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ ATTENDANCE RECORDING RULES                                              │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Recording attendance for any student-subject combination     │
 * │ ✓ ALLOWS: Multiple attendance records per day (different subjects)     │
 * │ ✓ ALLOWS: Updating existing attendance records                         │
 * │ ✓ ALLOWS: Recording attendance with any of the 4 status types          │
 * │ ✓ ALLOWS: Bulk recording for multiple students at once                 │
 * │ ✗ RESTRICTS: Recording without student ID                              │
 * │ ✗ RESTRICTS: Recording without subject ID                              │
 * │ ✗ RESTRICTS: Recording without teacher ID                              │
 * │ ⚠ NOTE: Duplicate records for same student-subject-date are allowed    │
 * │         (may need business logic to prevent in production)             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DATA ACCESS RULES                                                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Teachers to view attendance they recorded                    │
 * │ ✓ ALLOWS: Viewing attendance by subject                                │
 * │ ✓ ALLOWS: Viewing attendance by student                                │
 * │ ✓ ALLOWS: Viewing attendance by date                                   │
 * │ ✓ ALLOWS: Combined filters (subject + date, teacher + date, etc.)      │
 * │ ✓ ALLOWS: Statistical analysis of attendance data                      │
 * │ ✗ DOES NOT: Filter by course directly (use subject as proxy)           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DELETION RULES                                                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ⚠ WARNING: Deleting attendance records is permanent                    │
 * │ ⚠ IMPACT: Historical data will be LOST                                 │
 * │ ⚠ RECOMMENDATION: Consider soft-delete or archiving for production     │
 * │                                                                         │
 * │ CASCADE SOURCES (these deletions affect attendance):                    │
 * │   - Deleting a Student → All their attendance records deleted          │
 * │   - Deleting a Subject → All attendance for that subject deleted       │
 * │   - Deleting a Teacher → All attendance they recorded deleted          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param checkInsDao Data Access Object for attendance database operations
 */
class AttendanceRepository(
    private val checkInsDao: CheckInsDao
) {

    // Date/Time formatters for consistent date handling
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Retrieves all attendance records in the system.
     *
     * USE CASE: System-wide attendance report
     * ⚠ CAUTION: May return very large dataset - use with pagination
     *
     * @return Flow of all check-in records (reactive updates)
     */
    fun getAllCheckIns(): Flow<List<CheckIns>> {
        return checkInsDao.getAllCheckIns()
    }

    /**
     * Retrieves attendance records recorded by a specific teacher.
     *
     * BUSINESS LOGIC:
     * - Filters by teacherId (who recorded the attendance)
     * - Returns all subjects and dates for this teacher
     *
     * USE CASE: Teacher's attendance history, personal records
     *
     * @param teacherId Teacher's unique identifier
     * @return Flow of check-ins recorded by this teacher
     */
    fun getCheckInsByTeacher(teacherId: String): Flow<List<CheckIns>> {
        return checkInsDao.getCheckInsByTeacher(teacherId)
    }

    /**
     * Retrieves attendance records for a specific subject.
     *
     * BUSINESS LOGIC:
     * - Filters by subjectId (which class)
     * - Returns all students and dates for this subject
     *
     * USE CASE: Subject attendance report, class participation analysis
     *
     * @param subjectId Subject's unique identifier
     * @return Flow of check-ins for this subject
     */
    fun getCheckInsBySubject(subjectId: String): Flow<List<CheckIns>> {
        return checkInsDao.getCheckInsBySubject(subjectId)
    }

    /**
     * Retrieves attendance records for a specific student.
     *
     * USE CASE: Student attendance report, individual performance tracking
     *
     * @param studentId Student's unique identifier
     * @return Flow of check-ins for this student
     */
    fun getCheckInsByStudent(studentId: String): Flow<List<CheckIns>> {
        return checkInsDao.getCheckInsByStudent(studentId)
    }

    /**
     * Retrieves attendance records for a specific date.
     *
     * @param date Date in "yyyy-MM-dd" format
     * @return Flow of check-ins for this date
     */
    fun getCheckInsByDate(date: String): Flow<List<CheckIns>> {
        return checkInsDao.getCheckInsByDate(date)
    }

    /**
     * Retrieves attendance records for a specific subject and date.
     *
     * BUSINESS LOGIC: Intersection of subject and date filters
     * USE CASE: Daily attendance sheet for a specific class
     *
     * @param subjectId Subject's unique identifier
     * @param date Date in "yyyy-MM-dd" format
     * @return Flow of check-ins
     */
    fun getCheckInsBySubjectAndDate(subjectId: String, date: String): Flow<List<CheckIns>> {
        return checkInsDao.getCheckInsBySubjectAndDate(subjectId, date)
    }

    /**
     * Retrieves attendance records for a teacher on a specific date.
     *
     * USE CASE: Teacher's daily attendance overview
     *
     * @param date Date in "yyyy-MM-dd" format
     * @param teacherId Teacher's unique identifier
     * @return Flow of check-ins
     */
    fun getCheckInsByDateAndTeacher(date: String, teacherId: String): Flow<List<CheckIns>> {
        return checkInsDao.getCheckInsByDateAndTeacher(date, teacherId)
    }

    /**
     * Retrieves TODAY's attendance records for a teacher.
     *
     * BUSINESS LOGIC: Automatically uses current date
     * USE CASE: Dashboard today's attendance widget
     *
     * @param teacherId Teacher's unique identifier
     * @return Flow of today's check-ins
     */
    fun getTodayCheckIns(teacherId: String): Flow<List<CheckIns>> {
        val today = dateFormat.format(Date())
        return checkInsDao.getCheckInsByDateAndTeacher(today, teacherId)
    }

    /**
     * Retrieves a specific attendance record by ID.
     *
     * @param checkInId Check-in's unique identifier
     * @return CheckIn if found, null otherwise
     */
    suspend fun getCheckInById(checkInId: String): CheckIns? {
        return checkInsDao.getCheckInById(checkInId)
    }

    /**
     * Retrieves attendance records as a list (non-Flow) for a subject and date.
     *
     * USE CASE: One-time data fetch for reports
     *
     * @param subjectId Subject's unique identifier
     * @param date Date in "yyyy-MM-dd" format
     * @return List of check-ins
     */
    suspend fun getCheckInsBySubjectAndDateList(subjectId: String, date: String): List<CheckIns> {
        return checkInsDao.getCheckInsBySubjectAndDateList(subjectId, date)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Records a single attendance entry.
     *
     * BUSINESS LOGIC:
     * 1. Generates unique UUID for check-in
     * 2. Uses current date and time
     * 3. Sets specified or default status
     *
     * REQUIRED FIELDS:
     * - studentId: Who attended
     * - subjectId: What class
     * - teacherId: Who recorded
     *
     * DEFAULT VALUES:
     * - status: PRESENT (if not specified)
     * - date: Current date
     * - time: Current time
     *
     * @param studentId Student's unique identifier (required)
     * @param subjectId Subject's unique identifier (required)
     * @param teacherId Teacher's unique identifier (required)
     * @param status Attendance status (default: PRESENT)
     * @return AttendanceResult indicating success or failure
     */
    suspend fun recordAttendance(
        studentId: String,
        subjectId: String,
        teacherId: String,
        status: AttendanceStatus = AttendanceStatus.PRESENT
    ): AttendanceResult {
        // Validation
        if (studentId.isBlank()) {
            return AttendanceResult.Error("Student ID is required")
        }
        if (subjectId.isBlank()) {
            return AttendanceResult.Error("Subject ID is required")
        }
        if (teacherId.isBlank()) {
            return AttendanceResult.Error("Teacher ID is required")
        }

        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        val checkIn = CheckIns(
            checkInId = UUID.randomUUID().toString(),
            studentId = studentId,
            subjectId = subjectId,
            teacherId = teacherId,
            checkInTime = currentTime,
            checkInDate = currentDate,
            status = status
        )

        checkInsDao.insertCheckIn(checkIn)
        return AttendanceResult.Success(checkIn)
    }

    /**
     * Records attendance with a specific date (for backdating).
     *
     * USE CASE: Recording attendance for past dates
     * ⚠ CAUTION: Should be restricted in production to prevent fraud
     *
     * @param studentId Student's unique identifier
     * @param subjectId Subject's unique identifier
     * @param teacherId Teacher's unique identifier
     * @param date Specific date to record for
     * @param status Attendance status
     * @return AttendanceResult
     */
    suspend fun recordAttendanceForDate(
        studentId: String,
        subjectId: String,
        teacherId: String,
        date: Date,
        status: AttendanceStatus = AttendanceStatus.PRESENT
    ): AttendanceResult {
        if (studentId.isBlank() || subjectId.isBlank() || teacherId.isBlank()) {
            return AttendanceResult.Error("All IDs are required")
        }

        val formattedDate = dateFormat.format(date)
        val currentTime = timeFormat.format(Date())

        val checkIn = CheckIns(
            checkInId = UUID.randomUUID().toString(),
            studentId = studentId,
            subjectId = subjectId,
            teacherId = teacherId,
            checkInTime = currentTime,
            checkInDate = formattedDate,
            status = status
        )

        checkInsDao.insertCheckIn(checkIn)
        return AttendanceResult.Success(checkIn)
    }

    /**
     * Records attendance for multiple students at once.
     *
     * BUSINESS LOGIC:
     * 1. Creates check-in records for all specified students
     * 2. Uses same date, time, subject, and teacher for all
     * 3. Status can be customized per student via statusMap
     *
     * USE CASE: End of class bulk attendance submission
     *
     * @param studentIds List of student IDs
     * @param subjectId Subject's unique identifier
     * @param teacherId Teacher's unique identifier
     * @param statusMap Map of studentId to status (defaults to PRESENT)
     * @return BulkAttendanceResult
     */
    suspend fun recordBulkAttendance(
        studentIds: List<String>,
        subjectId: String,
        teacherId: String,
        statusMap: Map<String, AttendanceStatus> = emptyMap()
    ): BulkAttendanceResult {
        if (studentIds.isEmpty()) {
            return BulkAttendanceResult.Error("No students specified")
        }
        if (subjectId.isBlank() || teacherId.isBlank()) {
            return BulkAttendanceResult.Error("Subject and Teacher IDs are required")
        }

        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        val checkIns = studentIds.map { studentId ->
            CheckIns(
                checkInId = UUID.randomUUID().toString(),
                studentId = studentId,
                subjectId = subjectId,
                teacherId = teacherId,
                checkInTime = currentTime,
                checkInDate = currentDate,
                status = statusMap[studentId] ?: AttendanceStatus.PRESENT
            )
        }

        checkInsDao.insertCheckIns(checkIns)
        return BulkAttendanceResult.Success(checkIns.size)
    }

    /**
     * Updates an existing attendance record.
     *
     * ALLOWS:
     * - Changing attendance status
     * - Updating time
     *
     * DOES NOT ALLOW (through this method):
     * - Changing student
     * - Changing subject
     * - Changing teacher
     * - Changing date
     *
     * @param checkIn Updated check-in object
     */
    suspend fun updateAttendance(checkIn: CheckIns) {
        checkInsDao.updateCheckIn(checkIn)
    }

    /**
     * Deletes an attendance record.
     *
     * ⚠️ WARNING: This is a permanent deletion
     * ⚠️ IMPACT: Historical data will be lost
     *
     * @param checkIn Check-in to delete
     */
    suspend fun deleteAttendance(checkIn: CheckIns) {
        checkInsDao.deleteCheckIn(checkIn)
    }

    // ========================================================================
    // STATISTICS & REPORTING
    // ========================================================================

    /**
     * Calculates comprehensive attendance statistics for a teacher.
     *
     * STATISTICS INCLUDED:
     * - Total check-ins recorded
     * - Today's check-ins
     * - Count by status (present, absent, late, excused)
     * - Attendance rate percentage
     *
     * USE CASE: Dashboard statistics, performance reports
     *
     * @param teacherId Teacher's unique identifier
     * @return Flow of AttendanceStats
     */
    fun getAttendanceStats(teacherId: String): Flow<AttendanceStats> {
        return flow {
            val checkIns = checkInsDao.getCheckInsByTeacherList(teacherId)
            val today = dateFormat.format(Date())
            val todayCheckIns = checkIns.filter { it.checkInDate == today }

            val presentCount = checkIns.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = checkIns.count { it.status == AttendanceStatus.ABSENT }
            val lateCount = checkIns.count { it.status == AttendanceStatus.LATE }
            val excusedCount = checkIns.count { it.status == AttendanceStatus.EXCUSED }

            val totalRecords = checkIns.size
            val attendanceRate = if (totalRecords > 0) {
                ((presentCount + lateCount).toFloat() / totalRecords * 100).toInt()
            } else 0

            val stats = AttendanceStats(
                totalCheckIns = totalRecords,
                todayCheckIns = todayCheckIns.size,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                excusedCount = excusedCount,
                attendanceRate = attendanceRate
            )
            emit(stats)
        }
    }

    /**
     * Calculates attendance statistics for a specific subject.
     *
     * @param subjectId Subject's unique identifier
     * @return Flow of AttendanceStats
     */
    fun getSubjectAttendanceStats(subjectId: String): Flow<AttendanceStats> {
        return flow {
            val checkIns = checkInsDao.getCheckInsBySubjectList(subjectId)
            val today = dateFormat.format(Date())
            val todayCheckIns = checkIns.filter { it.checkInDate == today }

            val presentCount = checkIns.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = checkIns.count { it.status == AttendanceStatus.ABSENT }
            val lateCount = checkIns.count { it.status == AttendanceStatus.LATE }
            val excusedCount = checkIns.count { it.status == AttendanceStatus.EXCUSED }

            val totalRecords = checkIns.size
            val attendanceRate = if (totalRecords > 0) {
                ((presentCount + lateCount).toFloat() / totalRecords * 100).toInt()
            } else 0

            emit(AttendanceStats(
                totalCheckIns = totalRecords,
                todayCheckIns = todayCheckIns.size,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                excusedCount = excusedCount,
                attendanceRate = attendanceRate
            ))
        }
    }

    /**
     * Calculates attendance statistics for a specific student.
     *
     * @param studentId Student's unique identifier
     * @return Flow of AttendanceStats
     */
    fun getStudentAttendanceStats(studentId: String): Flow<AttendanceStats> {
        return flow {
            val checkIns = checkInsDao.getCheckInsByStudentList(studentId)
            val today = dateFormat.format(Date())
            val todayCheckIns = checkIns.filter { it.checkInDate == today }

            val presentCount = checkIns.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = checkIns.count { it.status == AttendanceStatus.ABSENT }
            val lateCount = checkIns.count { it.status == AttendanceStatus.LATE }
            val excusedCount = checkIns.count { it.status == AttendanceStatus.EXCUSED }

            val totalRecords = checkIns.size
            val attendanceRate = if (totalRecords > 0) {
                ((presentCount + lateCount).toFloat() / totalRecords * 100).toInt()
            } else 0

            emit(AttendanceStats(
                totalCheckIns = totalRecords,
                todayCheckIns = todayCheckIns.size,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                excusedCount = excusedCount,
                attendanceRate = attendanceRate
            ))
        }
    }

    // ========================================================================
    // UTILITY OPERATIONS
    // ========================================================================

    /**
     * Checks if attendance has already been recorded for a student-subject-date.
     *
     * USE CASE: Preventing duplicate attendance entries
     *
     * @param studentId Student's unique identifier
     * @param subjectId Subject's unique identifier
     * @param date Date in "yyyy-MM-dd" format
     * @return true if attendance exists, false otherwise
     */
    suspend fun hasAttendanceRecord(studentId: String, subjectId: String, date: String): Boolean {
        val records = checkInsDao.getCheckInsBySubjectAndDateList(subjectId, date)
        return records.any { it.studentId == studentId }
    }

    /**
     * Gets the current formatted date string.
     *
     * @return Today's date in "yyyy-MM-dd" format
     */
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    /**
     * Gets the current formatted time string.
     *
     * @return Current time in "HH:mm:ss" format
     */
    fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }
}

/**
 * Data class for attendance statistics.
 */
data class AttendanceStats(
    /** Total number of attendance records */
    val totalCheckIns: Int = 0,
    /** Number of check-ins recorded today */
    val todayCheckIns: Int = 0,
    /** Count of PRESENT status records */
    val presentCount: Int = 0,
    /** Count of ABSENT status records */
    val absentCount: Int = 0,
    /** Count of LATE status records */
    val lateCount: Int = 0,
    /** Count of EXCUSED status records */
    val excusedCount: Int = 0,
    /** Attendance rate percentage (present + late / total * 100) */
    val attendanceRate: Int = 0
)

/**
 * Sealed class for single attendance recording results.
 */
sealed class AttendanceResult {
    data class Success(val checkIn: CheckIns) : AttendanceResult()
    data class Error(val message: String) : AttendanceResult()
}

/**
 * Sealed class for bulk attendance recording results.
 */
sealed class BulkAttendanceResult {
    data class Success(val count: Int) : BulkAttendanceResult()
    data class Error(val message: String) : BulkAttendanceResult()
}

// ============================================================================
// VALIDATION & FEEDBACK TYPES
// ============================================================================

/**
 * Represents the readiness state for taking attendance.
 * Used to provide meaningful feedback to users about what's missing.
 */
data class AttendanceReadinessState(
    val isReady: Boolean,
    val hasStudents: Boolean,
    val hasSubjects: Boolean,
    val hasCourses: Boolean,
    val studentCount: Int,
    val subjectCount: Int,
    val courseCount: Int,
    val warnings: List<AttendanceWarning>,
    val blockers: List<AttendanceBlocker>
) {
    companion object {
        /**
         * Creates a readiness state from the provided counts.
         */
        fun fromCounts(
            studentCount: Int,
            subjectCount: Int,
            courseCount: Int
        ): AttendanceReadinessState {
            val warnings = mutableListOf<AttendanceWarning>()
            val blockers = mutableListOf<AttendanceBlocker>()

            // Check for blockers (cannot proceed)
            if (studentCount == 0) {
                blockers.add(AttendanceBlocker.NO_STUDENTS)
            }
            if (subjectCount == 0) {
                blockers.add(AttendanceBlocker.NO_SUBJECTS)
            }

            // Check for warnings (can proceed but with limitations)
            if (courseCount == 0 && studentCount > 0) {
                warnings.add(AttendanceWarning.NO_COURSES)
            }
            if (studentCount in 1..2) {
                warnings.add(AttendanceWarning.FEW_STUDENTS)
            }
            if (subjectCount == 1) {
                warnings.add(AttendanceWarning.SINGLE_SUBJECT)
            }

            return AttendanceReadinessState(
                isReady = blockers.isEmpty(),
                hasStudents = studentCount > 0,
                hasSubjects = subjectCount > 0,
                hasCourses = courseCount > 0,
                studentCount = studentCount,
                subjectCount = subjectCount,
                courseCount = courseCount,
                warnings = warnings,
                blockers = blockers
            )
        }
    }
}

/**
 * Warnings that don't block attendance but inform the user.
 */
enum class AttendanceWarning(val message: String, val suggestion: String) {
    NO_COURSES(
        message = "No courses created yet",
        suggestion = "Consider organizing students into courses for better management"
    ),
    FEW_STUDENTS(
        message = "You have very few students",
        suggestion = "Add more students to make attendance tracking meaningful"
    ),
    SINGLE_SUBJECT(
        message = "Only one subject available",
        suggestion = "Create more subjects to categorize attendance by class"
    )
}

/**
 * Blockers that prevent attendance from being taken.
 */
enum class AttendanceBlocker(val title: String, val message: String, val actionLabel: String) {
    NO_STUDENTS(
        title = "No Students Found",
        message = "You need to add students before you can take attendance. Students are required to record who is present or absent.",
        actionLabel = "Add Students"
    ),
    NO_SUBJECTS(
        title = "No Subjects Found",
        message = "You need to create at least one subject to record attendance. Subjects help categorize attendance by class or period.",
        actionLabel = "Create Subject"
    ),
    NOT_LOGGED_IN(
        title = "Not Logged In",
        message = "You must be logged in as a teacher to take attendance.",
        actionLabel = "Login"
    )
}

/**
 * Result of validating attendance prerequisites.
 */
sealed class AttendanceValidationResult {
    object Valid : AttendanceValidationResult()
    data class Invalid(
        val blockers: List<AttendanceBlocker>,
        val warnings: List<AttendanceWarning> = emptyList()
    ) : AttendanceValidationResult()

    val isValid: Boolean get() = this is Valid
}

/**
 * Feedback message for UI display.
 */
data class AttendanceFeedback(
    val type: FeedbackType,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val actionRoute: String? = null
)

/**
 * Types of feedback messages for styling in the UI.
 */
enum class FeedbackType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

/**
 * Extension function to convert blockers to feedback messages.
 */
fun AttendanceBlocker.toFeedback(actionRoute: String): AttendanceFeedback {
    return AttendanceFeedback(
        type = FeedbackType.ERROR,
        title = this.title,
        message = this.message,
        actionLabel = this.actionLabel,
        actionRoute = actionRoute
    )
}

/**
 * Extension function to convert warnings to feedback messages.
 */
fun AttendanceWarning.toFeedback(): AttendanceFeedback {
    return AttendanceFeedback(
        type = FeedbackType.WARNING,
        title = "Heads Up",
        message = "${this.message}. ${this.suggestion}"
    )
}

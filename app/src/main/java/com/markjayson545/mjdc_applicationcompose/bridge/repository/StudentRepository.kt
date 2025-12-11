package com.markjayson545.mjdc_applicationcompose.bridge.repository

import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.StudentDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.TeacherStudentCrossRefDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithAttendance
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.StudentWithTeachers
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherStudentCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * ============================================================================
 * STUDENT REPOSITORY
 * ============================================================================
 *
 * This repository manages all business logic related to student management
 * in the attendance system. Students can be assigned to multiple teachers
 * and enrolled in courses.
 *
 * KEY FEATURES:
 * - Student registration and management
 * - Multi-teacher assignment (many-to-many relationship)
 * - Course enrollment
 * - Attendance history tracking
 *
 * ENTITY RELATIONSHIPS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        STUDENT RELATIONSHIPS                            │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │   Teacher ←──[N:M]──→ Student (via TeacherStudentCrossRef)             │
 * │   Student ──[N:1]──→ Course (optional enrollment)                       │
 * │   Student ←──[1:N]──→ CheckIns (attendance records)                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * BUSINESS RULES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ STUDENT CREATION RULES                                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Creating students with first and last name                   │
 * │ ✓ ALLOWS: Optional middle name                                         │
 * │ ✓ ALLOWS: Optional course assignment at creation                       │
 * │ ✓ ALLOWS: Automatic teacher assignment on creation                     │
 * │ ✗ RESTRICTS: Blank first name or last name                             │
 * │ ✗ RESTRICTS: Student without at least one teacher assignment           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ TEACHER ASSIGNMENT RULES                                                │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Assigning student to multiple teachers                       │
 * │ ✓ ALLOWS: Removing student from a teacher's roster                     │
 * │ ✓ ALLOWS: Transferring student between teachers                        │
 * │ ✗ RESTRICTS: Duplicate teacher-student assignments                     │
 * │ ⚠ NOTE: Student can exist without teacher (orphaned - needs cleanup)   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DATA ACCESS RULES                                                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Teachers to view only their assigned students                │
 * │ ✓ ALLOWS: Viewing students by course                                   │
 * │ ✓ ALLOWS: Searching students by name                                   │
 * │ ✓ ALLOWS: Viewing student's attendance history                         │
 * │ ✗ DOES NOT: Allow viewing other teachers' students (unless shared)     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DELETION RULES                                                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ⚠ WARNING: Deleting a student will cascade to:                         │
 * │   - All teacher assignments (TeacherStudentCrossRef)                   │
 * │   - All attendance records (CheckIns)                                  │
 * │ ⚠ NOTE: Student deletion does NOT affect courses or teachers           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param studentDao Data Access Object for student database operations
 * @param teacherStudentCrossRefDao DAO for teacher-student relationships
 */
class StudentRepository(
    private val studentDao: StudentDao,
    private val teacherStudentCrossRefDao: TeacherStudentCrossRefDao
) {

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Retrieves all students in the system.
     *
     * USE CASE: Admin view, global student directory
     * ⚠ CAUTION: May return large dataset - use with pagination for production
     *
     * @return Flow of all students (reactive updates)
     */
    fun getAllStudents(): Flow<List<Student>> {
        return studentDao.getAllStudents()
    }

    /**
     * Retrieves students assigned to a specific teacher.
     *
     * BUSINESS LOGIC:
     * - Uses TeacherStudentCrossRef join table
     * - Only returns students explicitly assigned to the teacher
     *
     * USE CASE: Teacher's student roster, attendance sheet
     *
     * @param teacherId The teacher's unique identifier
     * @return Flow of students assigned to this teacher
     */
    fun getStudentsByTeacher(teacherId: String): Flow<List<Student>> {
        return studentDao.getStudentsByTeacher(teacherId)
    }

    /**
     * Retrieves students enrolled in a specific course.
     *
     * BUSINESS LOGIC:
     * - Filters students by their courseId field
     * - Students without course assignment are excluded
     *
     * USE CASE: Course roster, course-based attendance
     *
     * @param courseId The course's unique identifier
     * @return Flow of students enrolled in this course
     */
    fun getStudentsByCourse(courseId: String): Flow<List<Student>> {
        return studentDao.getStudentsByCourse(courseId)
    }

    /**
     * Retrieves a specific student by ID.
     *
     * @param studentId Student's unique identifier
     * @return Student if found, null otherwise
     */
    suspend fun getStudentById(studentId: String): Student? {
        return studentDao.getStudentById(studentId)
    }

    /**
     * Retrieves a student by ID as a reactive Flow.
     *
     * USE CASE: Real-time student profile updates
     *
     * @param studentId Student's unique identifier
     * @return Flow of student (null if not found)
     */
    fun getStudentByIdFlow(studentId: String): Flow<Student?> {
        return studentDao.getStudentByIdFlow(studentId)
    }

    /**
     * Retrieves a student with all assigned teachers.
     *
     * USE CASE: Student profile showing all teaching staff
     *
     * @param studentId Student's unique identifier
     * @return StudentWithTeachers containing student and teacher list
     */
    suspend fun getStudentWithTeachers(studentId: String): StudentWithTeachers? {
        return studentDao.getStudentWithTeachers(studentId)
    }

    /**
     * Retrieves a student with their complete attendance history.
     *
     * USE CASE: Student attendance report, performance tracking
     * INCLUDES: All check-in records across all subjects
     *
     * @param studentId Student's unique identifier
     * @return StudentWithAttendance containing student and all check-ins
     */
    suspend fun getStudentWithAttendance(studentId: String): StudentWithAttendance? {
        return studentDao.getStudentWithAttendance(studentId)
    }

    /**
     * Searches students by name.
     *
     * ALLOWS: Partial matching on first, middle, or last name
     * USE CASE: Quick student lookup, search functionality
     *
     * @param query Search term
     * @return Flow of matching students
     */
    fun searchStudents(query: String): Flow<List<Student>> {
        return studentDao.searchStudents(query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Creates a new student and assigns them to a teacher.
     *
     * BUSINESS LOGIC:
     * 1. Validates required fields (first name, last name)
     * 2. Generates unique masked ID for student (STUD-XXX)
     * 3. Creates student record
     * 4. Creates teacher-student relationship
     *
     * RESTRICTIONS:
     * - First name cannot be blank
     * - Last name cannot be blank
     * - Teacher ID is required for assignment
     *
     * ALLOWS:
     * - Optional middle name
     * - Optional course assignment
     *
     * @param firstName Student's first name (required)
     * @param middleName Student's middle name (optional)
     * @param lastName Student's last name (required)
     * @param courseId Course to enroll in (optional)
     * @param teacherId Teacher to assign to (required)
     * @return StudentCreationResult indicating success or failure
     */
    suspend fun createStudent(
        firstName: String,
        middleName: String,
        lastName: String,
        courseId: String?,
        teacherId: String
    ): StudentCreationResult {
        // Validation: Required fields
        if (firstName.isBlank()) {
            return StudentCreationResult.Error("First name is required")
        }
        if (lastName.isBlank()) {
            return StudentCreationResult.Error("Last name is required")
        }
        if (teacherId.isBlank()) {
            return StudentCreationResult.Error("Teacher assignment is required")
        }

        // Generate masked student ID (STUD-001, STUD-002, etc.)
        val studentId = generateStudentId()

        // Create student with generated masked ID
        val newStudent = Student(
            studentId = studentId,
            firstName = firstName.trim(),
            middleName = middleName.trim(),
            lastName = lastName.trim(),
            courseId = courseId?.takeIf { it.isNotBlank() }
        )

        // Insert student
        studentDao.insertStudent(newStudent)

        // Create teacher-student relationship
        val crossRef = TeacherStudentCrossRef(
            teacherId = teacherId,
            studentId = newStudent.studentId
        )
        teacherStudentCrossRefDao.insert(crossRef)

        return StudentCreationResult.Success(newStudent)
    }

    /**
     * Generates a unique masked student ID in the format STUD-XXX.
     *
     * The ID is generated by:
     * 1. Getting the max existing number from STUD-XXX formatted IDs
     * 2. Incrementing by 1
     * 3. Formatting with zero-padding (3 digits)
     *
     * @return A unique student ID like "STUD-001", "STUD-002", etc.
     */
    private suspend fun generateStudentId(): String {
        val maxNumber = studentDao.getMaxStudentIdNumber() ?: 0
        var nextNumber = maxNumber + 1
        var newId = "STUD-%03d".format(nextNumber)

        // Handle potential conflicts (race condition safety)
        while (studentDao.studentIdExists(newId)) {
            nextNumber++
            newId = "STUD-%03d".format(nextNumber)
        }

        return newId
    }

    /**
     * Updates student information.
     *
     * ALLOWS:
     * - Updating name fields
     * - Changing course enrollment
     *
     * DOES NOT:
     * - Change student ID
     * - Modify teacher assignments (use separate methods)
     *
     * @param student Updated student object
     */
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    /**
     * Deletes a student from the system.
     *
     * ⚠️ CASCADE EFFECTS:
     * - All teacher-student relationships will be deleted
     * - All attendance records will be deleted
     *
     * DOES NOT AFFECT:
     * - Teachers
     * - Courses
     * - Other students
     *
     * @param student Student to delete
     */
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    // ========================================================================
    // TEACHER ASSIGNMENT OPERATIONS
    // ========================================================================

    /**
     * Assigns a student to an additional teacher.
     *
     * BUSINESS LOGIC:
     * - Checks if assignment already exists to prevent duplicates
     * - Creates new TeacherStudentCrossRef record
     *
     * USE CASE: Sharing student between teachers, co-teaching scenarios
     *
     * RESTRICTIONS:
     * - Cannot create duplicate assignments
     *
     * @param studentId Student's unique identifier
     * @param teacherId Teacher's unique identifier
     * @return true if assigned, false if already assigned
     */
    suspend fun assignStudentToTeacher(studentId: String, teacherId: String): Boolean {
        val exists = teacherStudentCrossRefDao.exists(teacherId, studentId)
        if (exists) {
            return false // Already assigned
        }

        val crossRef = TeacherStudentCrossRef(
            teacherId = teacherId,
            studentId = studentId
        )
        teacherStudentCrossRefDao.insert(crossRef)
        return true
    }

    /**
     * Removes a student from a teacher's roster.
     *
     * BUSINESS LOGIC:
     * - Removes the TeacherStudentCrossRef record
     * - Student record remains intact
     * - Other teacher assignments remain intact
     *
     * USE CASE: Student transfer, end of term cleanup
     *
     * ⚠ WARNING: Student may become orphaned if this is their only teacher
     *
     * @param studentId Student's unique identifier
     * @param teacherId Teacher's unique identifier
     */
    suspend fun removeStudentFromTeacher(studentId: String, teacherId: String) {
        teacherStudentCrossRefDao.deleteByIds(teacherId, studentId)
    }

    /**
     * Checks if a student is assigned to a specific teacher.
     *
     * USE CASE: Authorization check, assignment validation
     *
     * @param studentId Student's unique identifier
     * @param teacherId Teacher's unique identifier
     * @return true if assigned, false otherwise
     */
    suspend fun isStudentAssignedToTeacher(studentId: String, teacherId: String): Boolean {
        return teacherStudentCrossRefDao.exists(teacherId, studentId)
    }

    /**
     * Gets the count of students assigned to a teacher.
     *
     * USE CASE: Dashboard statistics, workload assessment
     *
     * @param teacherId Teacher's unique identifier
     * @return Number of students assigned
     */
    suspend fun getStudentCountForTeacher(teacherId: String): Int {
        return teacherStudentCrossRefDao.getStudentCountForTeacher(teacherId)
    }
}

/**
 * Sealed class representing student creation operation results.
 */
sealed class StudentCreationResult {
    /**
     * Student created successfully.
     * @param student The newly created student
     */
    data class Success(val student: Student) : StudentCreationResult()

    /**
     * Student creation failed.
     * @param message Human-readable error description
     */
    data class Error(val message: String) : StudentCreationResult()
}


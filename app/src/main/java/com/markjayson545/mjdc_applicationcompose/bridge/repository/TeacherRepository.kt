package com.markjayson545.mjdc_applicationcompose.bridge.repository

import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.TeacherDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Teacher
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherWithAllData
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherWithStudents
import kotlinx.coroutines.flow.Flow

/**
 * ============================================================================
 * TEACHER REPOSITORY
 * ============================================================================
 *
 * This repository handles all business logic related to teacher management,
 * authentication, and authorization in the attendance system.
 *
 * KEY FEATURES:
 * - Teacher registration with validation
 * - Teacher authentication (login/logout)
 * - Teacher profile management
 * - Password security enforcement
 *
 * BUSINESS RULES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ AUTHENTICATION RULES                                                    │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Email/password login for registered teachers                 │
 * │ ✓ ALLOWS: New teacher registration with unique email                   │
 * │ ✗ RESTRICTS: Duplicate email registration                              │
 * │ ✗ RESTRICTS: Passwords less than 6 characters                          │
 * │ ✗ RESTRICTS: Empty first/last name fields                              │
 * │ ✗ RESTRICTS: Invalid email format (must contain @)                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DATA ACCESS RULES                                                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ✓ ALLOWS: Teachers to view their own profile                           │
 * │ ✓ ALLOWS: Teachers to update their own information                     │
 * │ ✓ ALLOWS: Viewing list of all teachers (for admin purposes)            │
 * │ ✓ ALLOWS: Searching teachers by name                                   │
 * │ ✗ DOES NOT: Allow password retrieval (security)                        │
 * │ ✗ DOES NOT: Allow teachers to modify other teacher's data              │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ DELETION RULES                                                          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ ⚠ WARNING: Deleting a teacher will cascade to:                         │
 * │   - All student assignments (TeacherStudentCrossRef)                   │
 * │   - All courses owned by the teacher                                   │
 * │   - All subjects created by the teacher                                │
 * │   - All attendance records linked to the teacher                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param teacherDao Data Access Object for teacher database operations
 */
class TeacherRepository(
    private val teacherDao: TeacherDao
) {

    // ========================================================================
    // AUTHENTICATION OPERATIONS
    // ========================================================================

    /**
     * Authenticates a teacher using email and password.
     *
     * BUSINESS LOGIC:
     * - Validates credentials against database
     * - Returns teacher object if credentials match
     * - Returns null if credentials are invalid
     *
     * DOES NOT:
     * - Store session tokens (handled by ViewModel)
     * - Log failed attempts (can be added for security)
     * - Implement rate limiting (should be added for production)
     *
     * @param email Teacher's registered email
     * @param password Teacher's password (plain text - should be hashed in production)
     * @return Teacher object if authenticated, null otherwise
     */
    suspend fun login(email: String, password: String): Teacher? {
        return teacherDao.login(email, password)
    }

    /**
     * Registers a new teacher in the system.
     *
     * BUSINESS LOGIC:
     * - Generates unique UUID for teacher ID
     * - Validates all required fields
     * - Checks for email uniqueness
     * - Password confirmation matching
     *
     * RESTRICTIONS:
     * - First name and last name cannot be blank
     * - Email must contain '@' symbol
     * - Password must be at least 6 characters
     * - Password and confirmation must match
     * - Email must not already exist in system
     *
     * @param firstName Teacher's first name (required)
     * @param middleName Teacher's middle name (optional)
     * @param lastName Teacher's last name (required)
     * @param email Teacher's email - must be unique (required)
     * @param password Teacher's password - min 6 chars (required)
     * @param confirmPassword Must match password (required)
     * @return RegistrationResult indicating success or failure with message
     */
    suspend fun register(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): RegistrationResult {
        // Validation: Required fields
        if (firstName.isBlank() || lastName.isBlank()) {
            return RegistrationResult.Error("First name and last name are required")
        }

        // Validation: Email format
        if (email.isBlank() || !email.contains("@")) {
            return RegistrationResult.Error("Please enter a valid email")
        }

        // Validation: Password strength
        if (password.length < 6) {
            return RegistrationResult.Error("Password must be at least 6 characters")
        }

        // Validation: Password confirmation
        if (password != confirmPassword) {
            return RegistrationResult.Error("Passwords do not match")
        }

        // Business Rule: Email uniqueness
        val emailExists = teacherDao.isEmailExists(email)
        if (emailExists) {
            return RegistrationResult.Error("Email already registered")
        }

        // Generate masked teacher ID (TEACH-001, TEACH-002, etc.)
        val teacherId = generateTeacherId()

        // Create new teacher with generated masked ID
        val newTeacher = Teacher(
            teacherId = teacherId,
            firstName = firstName.trim(),
            middleName = middleName.trim(),
            lastName = lastName.trim(),
            email = email.trim().lowercase(),
            password = password // TODO: Hash password for production
        )

        teacherDao.insertTeacher(newTeacher)
        return RegistrationResult.Success(newTeacher)
    }

    /**
     * Generates a unique masked teacher ID in the format TEACH-XXX.
     *
     * The ID is generated by:
     * 1. Getting the max existing number from TEACH-XXX formatted IDs
     * 2. Incrementing by 1
     * 3. Formatting with zero-padding (3 digits)
     *
     * @return A unique teacher ID like "TEACH-001", "TEACH-002", etc.
     */
    private suspend fun generateTeacherId(): String {
        val maxNumber = teacherDao.getMaxTeacherIdNumber() ?: 0
        var nextNumber = maxNumber + 1
        var newId = "TEACH-%03d".format(nextNumber)

        // Handle potential conflicts (race condition safety)
        while (teacherDao.teacherIdExists(newId)) {
            nextNumber++
            newId = "TEACH-%03d".format(nextNumber)
        }

        return newId
    }

    /**
     * Checks if an email is already registered in the system.
     *
     * USE CASE: Pre-registration validation to provide immediate feedback
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    suspend fun isEmailRegistered(email: String): Boolean {
        return teacherDao.isEmailExists(email)
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Retrieves all teachers in the system.
     *
     * ALLOWS: Viewing complete list of registered teachers
     * USE CASE: Admin dashboard, teacher directory
     *
     * @return Flow of all teachers (reactive updates)
     */
    fun getAllTeachers(): Flow<List<Teacher>> {
        return teacherDao.getAllTeachers()
    }

    /**
     * Retrieves a specific teacher by their ID.
     *
     * @param teacherId Unique identifier of the teacher
     * @return Flow of teacher (null if not found)
     */
    fun getTeacherById(teacherId: String): Flow<Teacher?> {
        return teacherDao.getTeacherByIdFlow(teacherId)
    }

    /**
     * Retrieves a teacher with all their assigned students.
     *
     * USE CASE: Teacher dashboard showing student roster
     *
     * @param teacherId Teacher's unique identifier
     * @return TeacherWithStudents containing teacher and student list
     */
    suspend fun getTeacherWithStudents(teacherId: String): TeacherWithStudents? {
        return teacherDao.getTeacherWithStudents(teacherId)
    }

    /**
     * Retrieves a teacher with all related data (students, courses, subjects).
     *
     * USE CASE: Comprehensive teacher profile view
     * INCLUDES: Students, courses, and all associated data
     *
     * @param teacherId Teacher's unique identifier
     * @return TeacherWithAllData containing complete data hierarchy
     */
    suspend fun getTeacherWithAllData(teacherId: String): TeacherWithAllData? {
        return teacherDao.getTeacherWithAllData(teacherId)
    }

    /**
     * Searches teachers by name.
     *
     * ALLOWS: Partial matching on first name, middle name, or last name
     *
     * @param query Search term
     * @return Flow of matching teachers
     */
    fun searchTeachers(query: String): Flow<List<Teacher>> {
        return teacherDao.searchTeachers(query)
    }

    // ========================================================================
    // WRITE OPERATIONS
    // ========================================================================

    /**
     * Updates teacher information.
     *
     * BUSINESS LOGIC:
     * - Allows updating profile information
     * - Email changes should be validated for uniqueness
     *
     * RESTRICTIONS:
     * - Cannot update another teacher's profile (enforced at ViewModel level)
     * - Email uniqueness must be maintained
     *
     * @param teacher Updated teacher object
     */
    suspend fun updateTeacher(teacher: Teacher) {
        teacherDao.updateTeacher(teacher)
    }

    /**
     * Deletes a teacher from the system.
     *
     * ⚠️ WARNING - CASCADE EFFECTS:
     * This operation will trigger cascade deletion of:
     * 1. All TeacherStudentCrossRef records (student assignments)
     * 2. All courses owned by this teacher
     * 3. All subjects created by this teacher
     * 4. All attendance records linked to this teacher
     *
     * BUSINESS RULE: Should only be performed by admin or the teacher themselves
     *
     * @param teacher Teacher to delete
     */
    suspend fun deleteTeacher(teacher: Teacher) {
        teacherDao.deleteTeacher(teacher)
    }
}

/**
 * Sealed class representing registration operation results.
 *
 * Provides type-safe result handling for registration attempts.
 */
sealed class RegistrationResult {
    /**
     * Registration succeeded.
     * @param teacher The newly created teacher
     */
    data class Success(val teacher: Teacher) : RegistrationResult()

    /**
     * Registration failed.
     * @param message Human-readable error description
     */
    data class Error(val message: String) : RegistrationResult()
}


/**
 * ============================================================================
 * APP PREFERENCES
 * ============================================================================
 *
 * SharedPreferences-based storage for app settings.
 * Uses a singleton pattern with Context for accessing preferences.
 *
 * STORED PREFERENCES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ KEY                    │ TYPE    │ DESCRIPTION                         │
 * ├────────────────────────┼─────────┼─────────────────────────────────────-┤
 * │ student_name_format    │ String  │ Format for displaying student names │
 * │ theme_mode             │ String  │ App theme (light/dark/system)       │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
package com.markjayson545.mjdc_applicationcompose.bridge.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enum representing different name format options for students.
 */
enum class StudentNameFormat(val displayName: String, val example: String) {
    FIRST_MIDDLE_LAST("First Middle Last", "John Michael Smith"),
    FIRST_LAST("First Last", "John Smith"),
    LAST_FIRST_MIDDLE("Last, First Middle", "Smith, John Michael"),
    LAST_FIRST_MI("Last, First M.", "Smith, John M."),
    LAST_FIRST("Last, First", "Smith, John"),
    FIRST_MI_LAST("First M. Last", "John M. Smith");

    companion object {
        fun fromString(value: String): StudentNameFormat {
            return entries.find { it.name == value } ?: FIRST_MIDDLE_LAST
        }
    }
}

/**
 * Singleton object for managing app preferences.
 */
object AppPreferences {
    private const val PREFS_NAME = "mjdc_attendance_prefs"
    private const val KEY_STUDENT_NAME_FORMAT = "student_name_format"
    private const val KEY_THEME_MODE = "theme_mode"

    private var sharedPreferences: SharedPreferences? = null

    // StateFlow for reactive updates
    private val _studentNameFormat = MutableStateFlow(StudentNameFormat.FIRST_MIDDLE_LAST)
    val studentNameFormat: StateFlow<StudentNameFormat> = _studentNameFormat.asStateFlow()

    /**
     * Initialize preferences with context.
     * Call this in Application.onCreate() or MainActivity.
     */
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadPreferences()
        }
    }

    private fun loadPreferences() {
        sharedPreferences?.let { prefs ->
            val formatString = prefs.getString(KEY_STUDENT_NAME_FORMAT, StudentNameFormat.FIRST_MIDDLE_LAST.name)
            _studentNameFormat.value = StudentNameFormat.fromString(formatString ?: StudentNameFormat.FIRST_MIDDLE_LAST.name)
        }
    }

    /**
     * Set the student name format preference.
     */
    fun setStudentNameFormat(format: StudentNameFormat) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_STUDENT_NAME_FORMAT, format.name)
            apply()
        }
        _studentNameFormat.value = format
    }

    /**
     * Get the current student name format.
     */
    fun getStudentNameFormat(): StudentNameFormat {
        return _studentNameFormat.value
    }

    /**
     * Format a student name based on the current preference.
     *
     * @param firstName Student's first name
     * @param middleName Student's middle name (can be empty)
     * @param lastName Student's last name
     * @return Formatted name string
     */
    fun formatStudentName(firstName: String, middleName: String, lastName: String): String {
        val middle = middleName.trim()
        val first = firstName.trim()
        val last = lastName.trim()

        return when (_studentNameFormat.value) {
            StudentNameFormat.FIRST_MIDDLE_LAST -> {
                if (middle.isNotEmpty()) "$first $middle $last"
                else "$first $last"
            }
            StudentNameFormat.FIRST_LAST -> "$first $last"
            StudentNameFormat.LAST_FIRST_MIDDLE -> {
                if (middle.isNotEmpty()) "$last, $first $middle"
                else "$last, $first"
            }
            StudentNameFormat.LAST_FIRST_MI -> {
                if (middle.isNotEmpty()) "$last, $first ${middle.first()}."
                else "$last, $first"
            }
            StudentNameFormat.LAST_FIRST -> "$last, $first"
            StudentNameFormat.FIRST_MI_LAST -> {
                if (middle.isNotEmpty()) "$first ${middle.first()}. $last"
                else "$first $last"
            }
        }
    }
}

/**
 * Extension function for Student model to get formatted name.
 * Import this and call student.formattedName
 */
fun com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student.formattedName(): String {
    return AppPreferences.formatStudentName(firstName, middleName, lastName)
}


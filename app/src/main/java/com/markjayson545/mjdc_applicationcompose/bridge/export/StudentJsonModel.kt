package com.markjayson545.mjdc_applicationcompose.bridge.export

import kotlinx.serialization.Serializable

/**
 * Data class for exporting/importing student data via JSON.
 * Contains only the essential name fields - studentId is auto-generated on import.
 */
@Serializable
data class StudentExportData(
    val firstName: String,
    val middleName: String,
    val lastName: String
) {
    /**
     * Returns the full name for display purposes (e.g., duplicate detection messages)
     */
    val fullName: String
        get() = "$firstName $middleName $lastName".trim()
}

/**
 * Wrapper class for the exported JSON file.
 * Contains metadata and the list of students.
 */
@Serializable
data class StudentsExportWrapper(
    val exportDate: String,
    val studentCount: Int,
    val students: List<StudentExportData>
)

/**
 * Result of an import operation.
 * Tracks success/failure counts and provides details about skipped students.
 */
data class ImportResult(
    val successCount: Int,
    val skippedCount: Int,
    val skippedNames: List<String>,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean
        get() = errorMessage == null

    val totalProcessed: Int
        get() = successCount + skippedCount
}


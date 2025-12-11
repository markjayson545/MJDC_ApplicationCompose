package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between Students and Subjects.
 * Tracks which students are enrolled in which subjects for attendance filtering.
 *
 * RELATIONSHIP:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │   Student ←──[N:M]──→ Subject (via StudentSubjectCrossRef)             │
 * │                                                                         │
 * │   A student can be enrolled in multiple subjects                        │
 * │   A subject can have multiple students enrolled                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * USE CASES:
 * - Filter attendance list to show only enrolled students for a subject
 * - Track student enrollments across subjects
 * - Allow selective attendance management per subject
 *
 * CASCADE DELETE:
 * - Deleting a Student removes all their enrollments
 * - Deleting a Subject removes all enrollments for that subject
 */
@Entity(
    tableName = "student_subject_cross_ref",
    primaryKeys = ["studentId", "subjectId"],
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId")]
)
data class StudentSubjectCrossRef(
    val studentId: String,
    val subjectId: String
)


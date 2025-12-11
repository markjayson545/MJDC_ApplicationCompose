package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between Courses and Subjects.
 * Allows subjects to be shared across multiple courses or unique to a course.
 */
@Entity(
    tableName = "course_subject_cross_ref",
    primaryKeys = ["courseId", "subjectId"],
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["courseId"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId"), Index("subjectId")]
)
data class CourseSubjectCrossRef(
    val courseId: String,
    val subjectId: String
)


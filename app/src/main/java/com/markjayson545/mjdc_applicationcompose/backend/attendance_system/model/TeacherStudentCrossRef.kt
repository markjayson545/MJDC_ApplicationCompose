package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between Teachers and Students.
 * Allows a student to be assigned to multiple teachers.
 */
@Entity(
    tableName = "teacher_student_cross_ref",
    primaryKeys = ["teacherId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["teacherId"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("teacherId"), Index("studentId")]
)
data class TeacherStudentCrossRef(
    val teacherId: String,
    val studentId: String
)


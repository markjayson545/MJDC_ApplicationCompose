package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
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
        ),
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["teacherId"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId"), Index("teacherId")]
)
data class CheckIns(
    @PrimaryKey
    val checkInId: String,
    val studentId: String,
    val subjectId: String,
    val teacherId: String,
    val checkInTime: String,
    val checkInDate: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}

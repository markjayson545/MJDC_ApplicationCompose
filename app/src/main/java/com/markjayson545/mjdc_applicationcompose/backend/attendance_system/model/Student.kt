package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["courseId"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("courseId")]
)
data class Student(
    @PrimaryKey
    val studentId: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val courseId: String?
) {
    val fullName: String
        get() = "$firstName $middleName $lastName".trim()
}

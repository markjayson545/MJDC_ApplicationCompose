package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey
    val teacherId: String = "",
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val email: String,
    val password: String = ""
) {
    val fullName: String
        get() = "$firstName $middleName $lastName".trim()
}


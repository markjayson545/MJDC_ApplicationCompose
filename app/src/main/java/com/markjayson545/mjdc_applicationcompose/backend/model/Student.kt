package com.markjayson545.mjdc_applicationcompose.backend.model

data class Student(
    val studentId: String = "",
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val course: String
)
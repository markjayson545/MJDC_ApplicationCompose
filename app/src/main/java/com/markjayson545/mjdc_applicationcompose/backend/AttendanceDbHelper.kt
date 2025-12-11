package com.markjayson545.mjdc_applicationcompose.backend

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AttendanceDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "attendanceDatabase.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_ATTENDANCE = "student"
        const val COLUMN_ID = "student_id"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_MIDDLE_NAME = "middle_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_COURSE = "course"

        // Employee
        const val TABLE_EMPLOYEES = "employee"
        const val COLUMN_EMP_ID = "employee_id"
        const val COLUMN_EMP_FIRST_NAME = "first_name"
        const val COLUMN_EMP_MIDDLE_NAME = "middle_name"
        const val COLUMN_EMP_LAST_NAME = "last_name"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"

        // Course
        const val TABLE_COURSE = "course"
        const val COLUMN_COURSE_ID = "course_id"
        const val COLUMN_COURSE_NAME = "course_name"
        const val COLUMN_COURSE_CODE = "course_code"
    }

    private val createAttendanceTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_ATTENDANCE (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_FIRST_NAME TEXT NOT NULL,
            $COLUMN_MIDDLE_NAME TEXT NOT NULL,
            $COLUMN_LAST_NAME TEXT NOT NULL,
            $COLUMN_COURSE TEXT NOT NULL
            )
    """.trimIndent()

    private val createEmployeeTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_EMPLOYEES (
            $COLUMN_EMP_ID TEXT PRIMARY KEY,
            $COLUMN_EMP_FIRST_NAME TEXT NOT NULL,
            $COLUMN_EMP_MIDDLE_NAME TEXT NOT NULL,
            $COLUMN_EMP_LAST_NAME TEXT NOT NULL,
            $COLUMN_USERNAME TEXT NOT NULL,
            $COLUMN_PASSWORD TEXT NOT NULL
            )
    """.trimIndent()

    private val createCourseTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_COURSE (
            $COLUMN_COURSE_ID TEXT PRIMARY KEY,
            $COLUMN_COURSE_NAME TEXT NOT NULL,
            $COLUMN_COURSE_CODE TEXT NOT NULL
            )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createAttendanceTable)
        db.execSQL(createEmployeeTable)
        db.execSQL(createCourseTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Handle database upgrade as needed
    }
}
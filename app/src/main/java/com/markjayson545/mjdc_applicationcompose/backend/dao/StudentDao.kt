package com.markjayson545.mjdc_applicationcompose.backend.dao

import com.markjayson545.mjdc_applicationcompose.backend.AttendanceDbHelper
import com.markjayson545.mjdc_applicationcompose.backend.model.Student

class StudentDao(private val databaseHelper: AttendanceDbHelper) {

    fun getStudentCount(): Long {
        val cursor = databaseHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${AttendanceDbHelper.TABLE_ATTENDANCE}", null
        )
        var count: Long = 0
        if (cursor.moveToFirst()) {
            count = cursor.getLong(0)
        }
        cursor.close()
        return count
    }

    fun insertStudent(
        firstName: String,
        middleName: String,
        lastName: String,
        course: String
    ): Long {
        val nextId = getStudentCount() + 1
        val maskId = "STU-$nextId"
        val values = android.content.ContentValues().apply {
            put(AttendanceDbHelper.COLUMN_ID, maskId)
            put(AttendanceDbHelper.COLUMN_FIRST_NAME, firstName)
            put(AttendanceDbHelper.COLUMN_MIDDLE_NAME, middleName)
            put(AttendanceDbHelper.COLUMN_LAST_NAME, lastName)
            put(AttendanceDbHelper.COLUMN_COURSE, course)
        }
        return databaseHelper.writableDatabase.insert(
            AttendanceDbHelper.TABLE_ATTENDANCE, null, values
        )
    }

    fun getAllStudents(): List<Student> {
        val students =
            mutableListOf<Student>()
        val cursor = databaseHelper.readableDatabase.query(
            AttendanceDbHelper.TABLE_ATTENDANCE,
            null, null, null, null, null, null
        )

        with(cursor) {
            while (moveToNext()) {
                val studentId = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_ID))
                val firstName =
                    getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_FIRST_NAME))
                val middleName =
                    getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_MIDDLE_NAME))
                val lastName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_LAST_NAME))
                val course = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE))

                students.add(
                    Student(
                        studentId,
                        firstName,
                        middleName,
                        lastName,
                        course
                    )
                )
            }
        }
        cursor.close()
        return students
    }

    fun updateStudent(
        id: String,
        firstName: String,
        middleName: String,
        lastName: String,
        course: String
    ): Int {
        val values = android.content.ContentValues().apply {
            put(AttendanceDbHelper.COLUMN_FIRST_NAME, firstName)
            put(AttendanceDbHelper.COLUMN_MIDDLE_NAME, middleName)
            put(AttendanceDbHelper.COLUMN_LAST_NAME, lastName)
            put(AttendanceDbHelper.COLUMN_COURSE, course)
        }
        val selection = "${AttendanceDbHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id)
        return databaseHelper.writableDatabase.update(
            AttendanceDbHelper.TABLE_ATTENDANCE, values, selection, selectionArgs
        )
    }

    fun deleteStudent(id: String): Int {
        val selection = "${AttendanceDbHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id)
        return databaseHelper.writableDatabase.delete(
            AttendanceDbHelper.TABLE_ATTENDANCE, selection, selectionArgs
        )
    }

}
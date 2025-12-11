package com.markjayson545.mjdc_applicationcompose.backend.dao

import android.content.ContentValues
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceDbHelper
import com.markjayson545.mjdc_applicationcompose.backend.model.Course

class CourseDao(private val databaseHelper: AttendanceDbHelper) {

    fun getRowCount(): Long {
        val cursor = databaseHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${AttendanceDbHelper.TABLE_COURSE}", null
        )
        var count: Long = 0
        if (cursor.moveToFirst()) {
            count = cursor.getLong(0)
        }
        cursor.close()
        return count
    }

    fun insertCourse(
        courseName: String,
        courseCode: String
    ): Long {
        val nextId = getRowCount() + 1
        val maskId = "CRS-$nextId"
        val values = ContentValues().apply {
            put(AttendanceDbHelper.COLUMN_COURSE_ID, maskId)
            put(AttendanceDbHelper.COLUMN_COURSE_NAME, courseName)
            put(AttendanceDbHelper.COLUMN_COURSE_CODE, courseCode)
        }
        return databaseHelper.writableDatabase.insert(
            AttendanceDbHelper.TABLE_COURSE, null, values
        )
    }

    fun getAllCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val cursor = databaseHelper.readableDatabase.query(
            AttendanceDbHelper.TABLE_COURSE,
            null,
            null,
            null,
            null,
            null,
            null
        )
        with(cursor) {
            while (moveToNext()) {
                val course = Course(
                    courseId = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_ID)),
                    courseName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_NAME)),
                    courseCode = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_CODE)),
                    courseDescription = ""
                )
                courses.add(course)
            }
        }
        cursor.close()
        return courses
    }

    fun getCourseById(courseId: String): Course? {
        val cursor = databaseHelper.readableDatabase.query(
            AttendanceDbHelper.TABLE_COURSE,
            null,
            "${AttendanceDbHelper.COLUMN_COURSE_ID} = ?",
            arrayOf(courseId),
            null,
            null,
            null
        )
        var course: Course? = null
        with(cursor) {
            if (moveToFirst()) {
                course = Course(
                    courseId = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_ID)),
                    courseName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_NAME)),
                    courseCode = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_CODE)),
                    courseDescription = ""
                )
            }
        }
        cursor.close()
        return course
    }

    fun updateCourse(
        courseId: String,
        courseName: String,
        courseCode: String
    ): Int {
        val values = ContentValues().apply {
            put(AttendanceDbHelper.COLUMN_COURSE_NAME, courseName)
            put(AttendanceDbHelper.COLUMN_COURSE_CODE, courseCode)
        }
        val selection = "${AttendanceDbHelper.COLUMN_COURSE_ID} = ?"
        val selectionArgs = arrayOf(courseId)
        return databaseHelper.writableDatabase.update(
            AttendanceDbHelper.TABLE_COURSE, values, selection, selectionArgs
        )
    }

    fun deleteCourse(courseId: String): Int {
        val selection = "${AttendanceDbHelper.COLUMN_COURSE_ID} = ?"
        val selectionArgs = arrayOf(courseId)
        return databaseHelper.writableDatabase.delete(
            AttendanceDbHelper.TABLE_COURSE, selection, selectionArgs
        )
    }
}

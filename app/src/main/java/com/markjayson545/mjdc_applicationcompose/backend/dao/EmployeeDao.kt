package com.markjayson545.mjdc_applicationcompose.backend.dao

import android.content.ContentValues
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceDbHelper
import com.markjayson545.mjdc_applicationcompose.backend.model.Employee

class EmployeeDao(private val databaseHelper: AttendanceDbHelper) {

    fun getEmployeeCount(): Long {
        val cursor = databaseHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${AttendanceDbHelper.TABLE_EMPLOYEES}", null
        )
        var count: Long = 0
        if (cursor.moveToFirst()) {
            count = cursor.getLong(0)
        }
        cursor.close()
        return count
    }

    fun insertEmployee(
        firstName: String,
        middleName: String,
        lastName: String,
        username: String,
        password: String
    ): Long {
        val nextId = getEmployeeCount() + 1
        val maskId = "EMP-$nextId"
        val values = ContentValues().apply {
            put(AttendanceDbHelper.COLUMN_EMP_ID, maskId)
            put(AttendanceDbHelper.COLUMN_EMP_FIRST_NAME, firstName)
            put(AttendanceDbHelper.COLUMN_EMP_MIDDLE_NAME, middleName)
            put(AttendanceDbHelper.COLUMN_EMP_LAST_NAME, lastName)
            put(AttendanceDbHelper.COLUMN_USERNAME, username)
            put(AttendanceDbHelper.COLUMN_PASSWORD, password)
        }
        return databaseHelper.writableDatabase.insert(
            AttendanceDbHelper.TABLE_EMPLOYEES, null, values
        )
    }

    fun getAllEmployees(): List<Employee> {
        val employees = mutableListOf<Employee>()
        val cursor = databaseHelper.readableDatabase.query(
            AttendanceDbHelper.TABLE_EMPLOYEES,
            null,
            null,
            null,
            null,
            null,
            null
        )
        with(cursor) {
            while (moveToNext()) {
                val employee = Employee(
                    employeeId = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_ID)),
                    firstName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_FIRST_NAME)),
                    middleName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_MIDDLE_NAME)),
                    lastName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_LAST_NAME)),
                    username = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_USERNAME)),
                    password = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_PASSWORD))
                )
                employees.add(employee)
            }
        }
        cursor.close()
        return employees
    }

    fun getEmployeeById(employeeId: String): Employee? {
        val cursor = databaseHelper.readableDatabase.query(
            AttendanceDbHelper.TABLE_EMPLOYEES,
            null,
            "${AttendanceDbHelper.COLUMN_EMP_ID} = ?",
            arrayOf(employeeId),
            null,
            null,
            null
        )
        var employee: Employee? = null
        with(cursor) {
            if (moveToFirst()) {
                employee = Employee(
                    employeeId = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_ID)),
                    firstName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_FIRST_NAME)),
                    middleName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_MIDDLE_NAME)),
                    lastName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_EMP_LAST_NAME)),
                    username = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_USERNAME)),
                    password = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_PASSWORD))
                )
            }
        }
        cursor.close()
        return employee
    }

    fun deleteEmployee(id: String): Int {
        val selection = "${AttendanceDbHelper.COLUMN_EMP_ID} = ?"
        val selectionArgs = arrayOf(id)
        return databaseHelper.writableDatabase.delete(
            AttendanceDbHelper.TABLE_EMPLOYEES, selection, selectionArgs
        )
    }

    fun updateEmployee(
        id: String,
        firstName: String,
        middleName: String,
        lastName: String,
        username: String,
        password: String
    ): Int {
        val values = ContentValues().apply {
            put(AttendanceDbHelper.COLUMN_EMP_FIRST_NAME, firstName)
            put(AttendanceDbHelper.COLUMN_EMP_MIDDLE_NAME, middleName)
            put(AttendanceDbHelper.COLUMN_EMP_LAST_NAME, lastName)
            put(AttendanceDbHelper.COLUMN_USERNAME, username)
            put(AttendanceDbHelper.COLUMN_PASSWORD, password)
        }
        val selection = "${AttendanceDbHelper.COLUMN_EMP_ID} = ?"
        val selectionArgs = arrayOf(id)
        return databaseHelper.writableDatabase.update(
            AttendanceDbHelper.TABLE_EMPLOYEES, values, selection, selectionArgs
        )
    }
}
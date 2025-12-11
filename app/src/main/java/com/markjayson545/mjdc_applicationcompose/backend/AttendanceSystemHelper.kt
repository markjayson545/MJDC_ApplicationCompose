package com.markjayson545.mjdc_applicationcompose.backend

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.CheckInsDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.CourseDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.CourseSubjectCrossRefDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.StudentDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.SubjectDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.TeacherDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.dao.TeacherStudentCrossRefDao
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CheckIns
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CourseSubjectCrossRef
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Teacher
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.TeacherStudentCrossRef

@Database(
    entities = [
        Teacher::class,
        Student::class,
        Course::class,
        Subject::class,
        CheckIns::class,
        TeacherStudentCrossRef::class,
        CourseSubjectCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AttendanceSystemDatabase : RoomDatabase() {

    abstract fun teacherDao(): TeacherDao
    abstract fun studentDao(): StudentDao
    abstract fun courseDao(): CourseDao
    abstract fun subjectDao(): SubjectDao
    abstract fun checkInsDao(): CheckInsDao
    abstract fun teacherStudentCrossRefDao(): TeacherStudentCrossRefDao
    abstract fun courseSubjectCrossRefDao(): CourseSubjectCrossRefDao

    companion object {
        private const val DATABASE_NAME = "attendance_system_database"

        @Volatile
        private var INSTANCE: AttendanceSystemDatabase? = null

        fun getInstance(context: Context): AttendanceSystemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AttendanceSystemDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

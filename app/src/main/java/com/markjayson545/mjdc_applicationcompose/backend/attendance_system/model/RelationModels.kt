package com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Compound model representing a Teacher with their assigned Students.
 */
data class TeacherWithStudents(
    @Embedded val teacher: Teacher,
    @Relation(
        parentColumn = "teacherId",
        entityColumn = "studentId",
        associateBy = Junction(TeacherStudentCrossRef::class)
    )
    val students: List<Student>
)

/**
 * Compound model representing a Student with their assigned Teachers.
 */
data class StudentWithTeachers(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "teacherId",
        associateBy = Junction(TeacherStudentCrossRef::class)
    )
    val teachers: List<Teacher>
)

/**
 * Compound model representing a Course with its Subjects.
 */
data class CourseWithSubjects(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "subjectId",
        associateBy = Junction(CourseSubjectCrossRef::class)
    )
    val subjects: List<Subject>
)

/**
 * Compound model representing a Subject with its Courses.
 */
data class SubjectWithCourses(
    @Embedded val subject: Subject,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "courseId",
        associateBy = Junction(CourseSubjectCrossRef::class)
    )
    val courses: List<Course>
)

/**
 * Compound model representing a Teacher with all their data.
 */
data class TeacherWithAllData(
    @Embedded val teacher: Teacher,
    @Relation(
        parentColumn = "teacherId",
        entityColumn = "studentId",
        associateBy = Junction(TeacherStudentCrossRef::class)
    )
    val students: List<Student>,
    @Relation(
        parentColumn = "teacherId",
        entityColumn = "teacherId"
    )
    val courses: List<Course>,
    @Relation(
        parentColumn = "teacherId",
        entityColumn = "teacherId"
    )
    val subjects: List<Subject>
)

/**
 * Compound model for Student with their attendance records.
 */
data class StudentWithAttendance(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "studentId"
    )
    val checkIns: List<CheckIns>
)

/**
 * Compound model for Subject with attendance records.
 */
data class SubjectWithAttendance(
    @Embedded val subject: Subject,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "subjectId"
    )
    val checkIns: List<CheckIns>
)

/**
 * Compound model for Course with its Students.
 */
data class CourseWithStudents(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "courseId"
    )
    val students: List<Student>
)

/**
 * Compound model representing a Student with Course information.
 */
data class StudentWithCourse(
    @Embedded val student: Student,
    @Relation(
        parentColumn = "courseId",
        entityColumn = "courseId"
    )
    val course: Course?
)

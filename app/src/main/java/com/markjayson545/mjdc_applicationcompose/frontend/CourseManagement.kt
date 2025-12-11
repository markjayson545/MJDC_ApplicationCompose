package com.markjayson545.mjdc_applicationcompose.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceDbHelper
import com.markjayson545.mjdc_applicationcompose.backend.dao.CourseDao
import com.markjayson545.mjdc_applicationcompose.backend.model.Course
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManagement(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingCourseId by remember { mutableStateOf("") }

    val courses = remember { mutableStateListOf<Course>() }
    val dbHelper = AttendanceDbHelper(context)
    val courseDao = CourseDao(dbHelper)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var courseToDelete by remember { mutableStateOf("") }

    // Load courses on initial composition
    LaunchedEffect(Unit) {
        scope.launch {
            val coursesList = withContext(Dispatchers.IO) {
                courseDao.getAllCourses()
            }
            courses.clear()
            courses.addAll(coursesList)
        }
    }

    fun resetForm() {
        courseName = ""
        courseCode = ""
        isEditMode = false
        editingCourseId = ""
    }

    fun saveCourse() {
        if (courseName.isNotEmpty() && courseCode.isNotEmpty()) {
            scope.launch {
                if (isEditMode) {
                    val result = withContext(Dispatchers.IO) {
                        courseDao.updateCourse(editingCourseId, courseName, courseCode)
                    }
                    if (result > 0) {
                        Toast.makeText(
                            context,
                            "Course updated successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val updatedCourses = withContext(Dispatchers.IO) {
                            courseDao.getAllCourses()
                        }
                        courses.clear()
                        courses.addAll(updatedCourses)
                    } else {
                        Toast.makeText(context, "Failed to update course.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    val status = withContext(Dispatchers.IO) {
                        courseDao.insertCourse(courseName = courseName, courseCode = courseCode)
                    }
                    if (status < 0) {
                        Toast.makeText(context, "Failed to save course.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Course saved successfully.", Toast.LENGTH_SHORT)
                            .show()
                        val updatedCourses = withContext(Dispatchers.IO) {
                            courseDao.getAllCourses()
                        }
                        courses.clear()
                        courses.addAll(updatedCourses)
                    }
                }
                resetForm()
                showBottomSheet = false
            }
        } else {
            Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    resetForm()
                    showBottomSheet = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Course")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (courses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text("No courses yet. Click the + button to add one!")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    itemsIndexed(courses) { index, course ->
                        ManagementItemCard(
                            itemNumber = (index + 1).toString(),
                            title = course.courseName,
                            subtitle = course.courseCode,
                            onEdit = {
                                courseName = course.courseName
                                courseCode = course.courseCode
                                editingCourseId = course.courseId
                                isEditMode = true
                                showBottomSheet = true
                            },
                            onDelete = {
                                courseToDelete = course.courseId
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for adding/editing
    InputBottomSheet(
        isVisible = showBottomSheet,
        title = if (isEditMode) "Edit Course" else "Add New Course",
        isEditMode = isEditMode,
        onDismiss = {
            showBottomSheet = false
            resetForm()
        },
        onSaveClick = { saveCourse() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ManagementTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = "Course Name",
                placeholder = "e.g., Mobile Application Development",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = courseCode,
                onValueChange = { courseCode = it },
                label = "Course Code",
                placeholder = "e.g., MD101",
                icon = Icons.Default.TextFields
            )
        }
    }

    // Delete confirmation dialog
    DeleteConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Course",
        message = "Are you sure you want to delete this course?",
        onConfirm = {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    courseDao.deleteCourse(courseToDelete)
                }
                if (result > 0) {
                    Toast.makeText(context, "Course deleted successfully.", Toast.LENGTH_SHORT)
                        .show()
                    val updatedCourses = withContext(Dispatchers.IO) {
                        courseDao.getAllCourses()
                    }
                    courses.clear()
                    courses.addAll(updatedCourses)
                } else {
                    Toast.makeText(context, "Failed to delete course.", Toast.LENGTH_SHORT).show()
                }
                showDeleteDialog = false
            }
        },
        onDismiss = {
            showDeleteDialog = false
            courseToDelete = ""
        }
    )
}
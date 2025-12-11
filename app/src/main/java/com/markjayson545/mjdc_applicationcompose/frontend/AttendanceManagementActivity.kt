package com.markjayson545.mjdc_applicationcompose.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.markjayson545.mjdc_applicationcompose.backend.dao.StudentDao
import com.markjayson545.mjdc_applicationcompose.backend.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceManagementActivity(navController: NavController) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingStudentId by remember { mutableStateOf("") }

    val students = remember { mutableStateListOf<Student>() }
    val dbHelper = AttendanceDbHelper(context)
    val studentDao = StudentDao(dbHelper)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutine.launch {
            val studentsList = withContext(Dispatchers.IO) {
                studentDao.getAllStudents()
            }
            students.clear()
            students.addAll(studentsList)
        }
    }

    fun resetForm() {
        firstName = ""
        middleName = ""
        lastName = ""
        course = ""
        isEditMode = false
        editingStudentId = ""
    }

    fun saveStudent() {
        if (firstName.isNotEmpty() && middleName.isNotEmpty() && lastName.isNotEmpty() && course.isNotEmpty()) {
            coroutine.launch {
                if (isEditMode) {
                    val result = withContext(Dispatchers.IO) {
                        studentDao.updateStudent(
                            editingStudentId, firstName, middleName, lastName, course
                        )
                    }
                    if (result > 0) {
                        Toast.makeText(context, "Student updated successfully!", Toast.LENGTH_LONG)
                            .show()
                        val updatedStudents = withContext(Dispatchers.IO) {
                            studentDao.getAllStudents()
                        }
                        students.clear()
                        students.addAll(updatedStudents)
                    } else {
                        Toast.makeText(context, "Failed to update student.", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    val status = withContext(Dispatchers.IO) {
                        studentDao.insertStudent(firstName, middleName, lastName, course)
                    }
                    if (status < 0) {
                        Toast.makeText(
                            context,
                            "Failed to save student. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Student saved successfully!", Toast.LENGTH_LONG)
                            .show()
                        val updatedStudents = withContext(Dispatchers.IO) {
                            studentDao.getAllStudents()
                        }
                        students.clear()
                        students.addAll(updatedStudents)
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
                title = { Text("Student Management") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (students.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, null)
                    Text("No students yet. Click the + button to add one!")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    itemsIndexed(students) { index, student ->
                        ManagementItemCard(
                            itemNumber = (index + 1).toString(),
                            title = "${student.firstName} ${student.middleName} ${student.lastName}",
                            subtitle = student.course,
                            onEdit = {
                                firstName = student.firstName
                                middleName = student.middleName
                                lastName = student.lastName
                                course = student.course
                                editingStudentId = student.studentId
                                isEditMode = true
                                showBottomSheet = true
                            },
                            onDelete = {
                                studentToDelete = student.studentId
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
        title = if (isEditMode) "Edit Student" else "Add New Student",
        isEditMode = isEditMode,
        onDismiss = {
            showBottomSheet = false
            resetForm()
        },
        onSaveClick = { saveStudent() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ManagementTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                placeholder = "e.g., Mark",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = middleName,
                onValueChange = { middleName = it },
                label = "Middle Name",
                placeholder = "e.g., Vinluan",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Last Name",
                placeholder = "e.g., Dela Cruz",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = course,
                onValueChange = { course = it },
                label = "Course Name",
                placeholder = "e.g., Bachelor of Science in IT",
                icon = Icons.Default.TextFields
            )
        }
    }

    // Delete confirmation dialog
    DeleteConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Student",
        message = "Are you sure you want to delete this student?",
        onConfirm = {
            coroutine.launch {
                val result = withContext(Dispatchers.IO) {
                    studentDao.deleteStudent(studentToDelete)
                }
                if (result > 0) {
                    Toast.makeText(context, "Student deleted successfully.", Toast.LENGTH_SHORT)
                        .show()
                    val updatedStudents = withContext(Dispatchers.IO) {
                        studentDao.getAllStudents()
                    }
                    students.clear()
                    students.addAll(updatedStudents)
                } else {
                    Toast.makeText(context, "Failed to delete student.", Toast.LENGTH_SHORT).show()
                }
                showDeleteDialog = false
            }
        },
        onDismiss = {
            showDeleteDialog = false
            studentToDelete = ""
        }
    )
}
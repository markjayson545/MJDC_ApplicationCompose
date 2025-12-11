package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactItemCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DeleteConfirmationDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.InfoBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SearchBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.StudentFormDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SuccessBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator.DrawerDestinations
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyExitTransition
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStudentsScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {
    val teacherViewModel = sharedViewModels.teacherViewModel
    val studentViewModel = sharedViewModels.studentViewModel
    val courseViewModel = sharedViewModels.courseViewModel

    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val isLoading by studentViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }

    var previousStudentCount by remember { mutableIntStateOf(students.size) }
    LaunchedEffect(students.size) {
        if (students.size > previousStudentCount) {
            successMessage = "Student added successfully!"
            showSuccessBanner = true
            delay(3000)
            showSuccessBanner = false
        }
        previousStudentCount = students.size
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.studentId.contains(searchQuery, ignoreCase = true)
        }
    }

    val hasCourses = courses.isNotEmpty()

    LaunchedEffect(currentTeacher) {
        currentTeacher?.let {
            studentViewModel.loadStudentsByTeacher(it.teacherId)
            courseViewModel.loadCoursesByTeacher(it.teacherId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Students",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${students.size} student${if (students.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = showSuccessBanner,
                enter = bouncyEnterTransition(fromBottom = false),
                exit = bouncyExitTransition(toBottom = false)
            ) {
                SuccessBanner(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    title = "Success!",
                    message = successMessage,
                    onDismiss = { showSuccessBanner = false }
                )
            }

            AnimatedVisibility(
                visible = !hasCourses && students.isEmpty() && showContent,
                enter = bouncyEnterTransition(fromBottom = false),
                exit = bouncyExitTransition(toBottom = false)
            ) {
                InfoBanner(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    title = "Tip: Create Courses First",
                    message = "Creating courses helps you organize students.",
                    actionLabel = "Create Course",
                    onActionClick = { navController.navigate(DrawerDestinations.MANAGE_COURSES.route) }
                )
            }

            AnimatedVisibility(
                visible = showContent,
                enter = bouncyEnterTransition(fromBottom = false)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search students...",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredStudents.isEmpty()) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition()
                ) {
                    if (searchQuery.isNotEmpty()) {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Search,
                            title = "No Students Found",
                            subtitle = "No students match \"$searchQuery\""
                        )
                    } else {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Groups,
                            title = "No Students Yet",
                            subtitle = "Add your first student to get started",
                            actionLabel = "Add Student",
                            onActionClick = { showAddDialog = true }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        filteredStudents,
                        key = { _, student -> student.studentId }) { index, student ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = bouncyEnterTransition(index = index)
                        ) {
                            val course = courses.find { it.courseId == student.courseId }
                            CompactItemCard(
                                icon = Icons.Default.Person,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = student.fullName,
                                subtitle = student.studentId,
                                badge = course?.courseCode,
                                onEdit = {
                                    selectedStudent = student
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedStudent = student
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        StudentFormDialog(
            title = "Add Student",
            courses = courses,
            onDismiss = { showAddDialog = false },
            onConfirm = { firstName, middleName, lastName, courseId ->
                currentTeacher?.let {
                    studentViewModel.addStudent(
                        firstName = firstName,
                        middleName = middleName,
                        lastName = lastName,
                        courseId = courseId,
                        teacherId = it.teacherId
                    )
                }
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && selectedStudent != null) {
        StudentFormDialog(
            title = "Edit Student",
            student = selectedStudent,
            courses = courses,
            onDismiss = {
                showEditDialog = false
                selectedStudent = null
            },
            onConfirm = { firstName, middleName, lastName, courseId ->
                selectedStudent?.let {
                    studentViewModel.updateStudent(
                        it.copy(
                            firstName = firstName,
                            middleName = middleName,
                            lastName = lastName,
                            courseId = courseId
                        )
                    )
                }
                showEditDialog = false
                selectedStudent = null
            }
        )
    }

    if (showDeleteDialog && selectedStudent != null) {
        DeleteConfirmationDialog(
            itemName = selectedStudent?.fullName ?: "",
            itemType = "Student",
            onConfirm = {
                selectedStudent?.let { studentViewModel.deleteStudent(it) }
                showDeleteDialog = false
                selectedStudent = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedStudent = null
            }
        )
    }
}
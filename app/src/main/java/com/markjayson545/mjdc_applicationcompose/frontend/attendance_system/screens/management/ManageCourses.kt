package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.management

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactItemCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CourseSortOption
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CourseFormDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CourseSortBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DeleteConfirmationDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.FilterSortRow
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.InfoBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SearchBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SuccessBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyExitTransition
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ManageCoursesScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {

    val teacherViewModel = sharedViewModels.teacherViewModel
    val courseViewModel = sharedViewModels.courseViewModel
    val studentViewModel = sharedViewModels.studentViewModel

    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val isLoading by courseViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }

    // Sort state (not persistent - in-memory only)
    var sortOption by remember { mutableStateOf(CourseSortOption.NAME_ASC) }
    var showSortSheet by remember { mutableStateOf(false) }

    var previousCourseCount by remember { mutableStateOf(courses.size) }
    LaunchedEffect(courses.size) {
        if (courses.size > previousCourseCount) {
            successMessage = "Course created successfully!"
            showSuccessBanner = true
            delay(3000)
            showSuccessBanner = false
        }
        previousCourseCount = courses.size
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    fun getStudentCountForCourse(courseId: String): Int {
        return students.count { it.courseId == courseId }
    }

    val filteredCourses = remember(courses, searchQuery, sortOption, students) {
        var list = if (searchQuery.isBlank()) courses
        else courses.filter {
            it.courseName.contains(searchQuery, ignoreCase = true) ||
                    it.courseCode.contains(searchQuery, ignoreCase = true)
        }

        // Apply sorting
        when (sortOption) {
            CourseSortOption.NAME_ASC -> list.sortedBy { it.courseName }
            CourseSortOption.NAME_DESC -> list.sortedByDescending { it.courseName }
            CourseSortOption.STUDENT_COUNT_ASC -> list.sortedBy { getStudentCountForCourse(it.courseId) }
            CourseSortOption.STUDENT_COUNT_DESC -> list.sortedByDescending { getStudentCountForCourse(it.courseId) }
        }
    }

    LaunchedEffect(currentTeacher) {
        currentTeacher?.let {
            courseViewModel.loadCoursesByTeacher(it.teacherId)
            studentViewModel.loadStudentsByTeacher(it.teacherId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Courses",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${courses.size} course${if (courses.size != 1) "s" else ""}",
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
                visible = courses.isEmpty() && showContent,
                enter = bouncyEnterTransition(fromBottom = false),
                exit = bouncyExitTransition(toBottom = false)
            ) {
                InfoBanner(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    title = "Getting Started",
                    message = "Courses help you organize students and subjects."
                )
            }

            AnimatedVisibility(
                visible = showContent,
                enter = bouncyEnterTransition(fromBottom = false)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search courses...",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                )
            }

            // Sort Row
            AnimatedVisibility(
                visible = showContent && courses.isNotEmpty(),
                enter = bouncyEnterTransition(fromBottom = false)
            ) {
                FilterSortRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    hasActiveFilters = false,
                    activeFilterCount = 0,
                    onFilterClick = { /* No filters for courses */ },
                    sortLabel = sortOption.label,
                    onSortClick = { showSortSheet = true }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator()
                    Text(
                        "Loading...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (filteredCourses.isEmpty()) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition()
                ) {
                    if (searchQuery.isNotEmpty()) {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Search,
                            title = "No Courses Found",
                            subtitle = "No courses match \"$searchQuery\""
                        )
                    } else {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Book,
                            title = "No Courses Yet",
                            subtitle = "Create your first course to organize students",
                            actionLabel = "Create Course",
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
                        filteredCourses,
                        key = { _, course -> course.courseId }) { index, course ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = bouncyEnterTransition(index = index)
                        ) {
                            val studentCount = getStudentCountForCourse(course.courseId)
                            CompactItemCard(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                iconTint = Color(0xFF9C27B0),
                                title = course.courseName,
                                subtitle = "${course.courseCode} • $studentCount student${if (studentCount != 1) "s" else ""}",
                                onEdit = {
                                    selectedCourse = course
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedCourse = course
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
        CourseFormDialog(
            title = "Add Course",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, code ->
                currentTeacher?.let {
                    courseViewModel.addCourse(
                        courseName = name,
                        courseCode = code,
                        teacherId = it.teacherId
                    )
                }
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && selectedCourse != null) {
        CourseFormDialog(
            title = "Edit Course",
            course = selectedCourse,
            onDismiss = {
                showEditDialog = false
                selectedCourse = null
            },
            onConfirm = { name, code ->
                selectedCourse?.let {
                    courseViewModel.updateCourse(
                        it.copy(
                            courseName = name,
                            courseCode = code
                        )
                    )
                }
                showEditDialog = false
                selectedCourse = null
            }
        )
    }

    if (showDeleteDialog && selectedCourse != null) {
        DeleteConfirmationDialog(
            itemName = selectedCourse?.courseName ?: "",
            itemType = "Course",
            additionalMessage = "This will also affect associated students.",
            onConfirm = {
                selectedCourse?.let { courseViewModel.deleteCourse(it) }
                showDeleteDialog = false
                selectedCourse = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedCourse = null
            }
        )
    }

    // Sort Bottom Sheet
    CourseSortBottomSheet(
        isVisible = showSortSheet,
        currentSort = sortOption,
        onSortSelected = { sortOption = it },
        onDismiss = { showSortSheet = false }
    )
}
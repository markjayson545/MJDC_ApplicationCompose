package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.management

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.AppPreferences
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.formattedName
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactItemCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DeleteConfirmationDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.ExportResultDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.FilterSortRow
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.ImportExportBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.ImportResultDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.InfoBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SearchBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.StudentEnrollmentBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.StudentFilterBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.StudentFormDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.StudentSortBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.StudentSortOption
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
    val context = LocalContext.current

    // Initialize AppPreferences for name formatting
    LaunchedEffect(Unit) {
        AppPreferences.init(context)
    }

    val teacherViewModel = sharedViewModels.teacherViewModel
    val studentViewModel = sharedViewModels.studentViewModel
    val courseViewModel = sharedViewModels.courseViewModel
    val subjectViewModel = sharedViewModels.subjectViewModel
    val enrollmentViewModel = sharedViewModels.enrollmentViewModel

    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val subjects by subjectViewModel.teacherSubjects.collectAsState()
    val isLoading by studentViewModel.isLoading.collectAsState()
    val enrollmentCounts by enrollmentViewModel.studentEnrollmentCounts.collectAsState()
    val enrolledSubjectIds by enrollmentViewModel.enrolledSubjectIds.collectAsState()
    val importResult by studentViewModel.importResult.collectAsState()
    val exportSuccess by studentViewModel.exportSuccess.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }
    var showEnrollmentSheet by remember { mutableStateOf(false) }
    var showImportExportSheet by remember { mutableStateOf(false) }
    var selectedCourseIdForImport by remember { mutableStateOf<String?>(null) }

    // Filter & Sort state (not persistent - in-memory only)
    var selectedCourseId by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(StudentSortOption.NAME_ASC) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

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

    val filteredStudents = remember(students, searchQuery, selectedCourseId, sortOption) {
        var list = students

        // Apply search filter (search in both formatted name and raw fields)
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.formattedName().contains(searchQuery, ignoreCase = true) ||
                        it.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.studentId.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply course filter
        if (selectedCourseId != null) {
            list = list.filter { it.courseId == selectedCourseId }
        }

        // Apply sorting using formattedName for name-based sorting
        when (sortOption) {
            StudentSortOption.NAME_ASC -> list.sortedBy { it.formattedName().lowercase() }
            StudentSortOption.NAME_DESC -> list.sortedByDescending { it.formattedName().lowercase() }
            StudentSortOption.ID_ASC -> list.sortedBy { it.studentId }
            StudentSortOption.ID_DESC -> list.sortedByDescending { it.studentId }
        }
    }

    // Calculate active filter count
    val activeFilterCount = if (selectedCourseId != null) 1 else 0

    val hasCourses = courses.isNotEmpty()

    // File launchers for import/export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                studentViewModel.exportStudents(students, outputStream)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { inputStream ->
                currentTeacher?.let { teacher ->
                    studentViewModel.importStudents(inputStream, teacher.teacherId, selectedCourseIdForImport)
                }
            }
        }
    }

    LaunchedEffect(currentTeacher) {
        currentTeacher?.let {
            studentViewModel.loadStudentsByTeacher(it.teacherId)
            courseViewModel.loadCoursesByTeacher(it.teacherId)
            subjectViewModel.loadSubjectsByTeacher(it.teacherId)
        }
    }

    // Load enrollment counts when students change
    LaunchedEffect(students) {
        if (students.isNotEmpty()) {
            enrollmentViewModel.loadEnrollmentCountsForStudents(students)
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
                actions = {
                    IconButton(onClick = { showImportExportSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Import/Export students"
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
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                )
            }

            // Filter and Sort Row
            AnimatedVisibility(
                visible = showContent && (courses.isNotEmpty() || students.isNotEmpty()),
                enter = bouncyEnterTransition(fromBottom = false)
            ) {
                FilterSortRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    hasActiveFilters = activeFilterCount > 0,
                    activeFilterCount = activeFilterCount,
                    onFilterClick = { showFilterSheet = true },
                    sortLabel = sortOption.label,
                    onSortClick = { showSortSheet = true }
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
                            val enrollmentCount = enrollmentCounts[student.studentId] ?: 0
                            CompactItemCard(
                                icon = Icons.Default.Person,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = student.formattedName(),
                                subtitle = student.studentId,
                                badge = course?.courseCode,
                                secondaryBadge = if (enrollmentCount > 0) "$enrollmentCount subject${if (enrollmentCount != 1) "s" else ""}" else null,
                                onEnroll = {
                                    selectedStudent = student
                                    enrollmentViewModel.loadEnrolledSubjectsForStudent(student.studentId)
                                    showEnrollmentSheet = true
                                },
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

    // Filter Bottom Sheet
    StudentFilterBottomSheet(
        isVisible = showFilterSheet,
        courses = courses,
        selectedCourseId = selectedCourseId,
        onCourseSelected = { selectedCourseId = it },
        onDismiss = { showFilterSheet = false },
        onClearFilters = {
            selectedCourseId = null
            showFilterSheet = false
        }
    )

    // Sort Bottom Sheet
    StudentSortBottomSheet(
        isVisible = showSortSheet,
        currentSort = sortOption,
        onSortSelected = { sortOption = it },
        onDismiss = { showSortSheet = false }
    )

    // Enrollment Bottom Sheet
    StudentEnrollmentBottomSheet(
        isVisible = showEnrollmentSheet,
        student = selectedStudent,
        subjects = subjects,
        enrolledSubjectIds = enrolledSubjectIds,
        onSave = { newSubjectIds ->
            selectedStudent?.let { student ->
                enrollmentViewModel.updateStudentEnrollments(student.studentId, newSubjectIds)
                successMessage = "Enrollment updated successfully!"
                showSuccessBanner = true
            }
            showEnrollmentSheet = false
            selectedStudent = null
        },
        onDismiss = {
            showEnrollmentSheet = false
            selectedStudent = null
            enrollmentViewModel.clearEnrolledSubjectIds()
        }
    )

    // Import/Export Bottom Sheet
    ImportExportBottomSheet(
        isVisible = showImportExportSheet,
        courses = courses,
        hasStudents = students.isNotEmpty(),
        onExportClick = {
            exportLauncher.launch(studentViewModel.generateExportFilename())
        },
        onImportClick = { courseId ->
            selectedCourseIdForImport = courseId
            importLauncher.launch(arrayOf("application/json"))
        },
        onDismiss = { showImportExportSheet = false }
    )

    // Import Result Dialog
    ImportResultDialog(
        importResult = importResult,
        onDismiss = {
            studentViewModel.clearImportResult()
            if (importResult?.successCount ?: 0 > 0) {
                successMessage = "${importResult?.successCount} student${if ((importResult?.successCount ?: 0) != 1) "s" else ""} imported!"
                showSuccessBanner = true
            }
        }
    )

    // Export Result Dialog
    ExportResultDialog(
        success = exportSuccess,
        onDismiss = {
            studentViewModel.clearExportSuccess()
            if (exportSuccess == true) {
                successMessage = "Students exported successfully!"
                showSuccessBanner = true
            }
        }
    )
}
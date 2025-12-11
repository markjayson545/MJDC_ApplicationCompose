
/**
 * ============================================================================
 * MANAGE ATTENDANCE SCREEN
 * ============================================================================
 *
 * This is the primary attendance management screen where teachers record
 * student check-ins. It's the core functionality of the attendance system.
 *
 * SCREEN RESPONSIBILITIES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Display student list with attendance status (present/absent)        │
 * │ 2. Allow recording new check-ins via bottom sheet                      │
 * │ 3. Filter students by status (All, Present, Absent)                    │
 * │ 4. Select date for viewing attendance records                          │
 * │ 5. Validate readiness (requires students + subjects before recording)  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * DATA FLOW:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SharedViewModels                                                       │
 * │       │                                                                 │
 * │       ├── teacherViewModel.currentTeacher → Teacher context            │
 * │       ├── studentViewModel.teacherStudents → Student roster            │
 * │       ├── subjectViewModel.teacherSubjects → Subject selection         │
 * │       ├── courseViewModel.teacherCourses → Readiness validation        │
 * │       └── attendanceViewModel                                           │
 * │            ├── todayCheckIns → Today's attendance records              │
 * │            └── recordAttendance() → Create new check-in                │
 * │                                                                         │
 * │  REPOSITORY METHODS USED:                                               │
 * │  - AttendanceRepository.recordAttendance() for single check-ins        │
 * │  - AttendanceRepository.getTodayCheckIns() for current status          │
 * │  - AttendanceReadinessState.fromCounts() for setup validation          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * UI SECTIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Top App Bar - Title with present/absent counts                      │
 * │ 2. Setup Blocker (if !readinessState.isReady)                          │
 * │    - Shows which items need setup (students, subjects)                 │
 * │ 3. Date Filter Bar - Select date for attendance view                   │
 * │ 4. Tab Row - Filter by All, Present, Absent                            │
 * │ 5. Student List - Cards showing attendance status                      │
 * │ 6. FAB - Opens check-in bottom sheet                                   │
 * │ 7. Bottom Sheet - Subject selection + multi-student check-in           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * CHECK-IN FLOW:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. User taps FAB → Bottom sheet opens                                  │
 * │ 2. User selects subject from dropdown                                  │
 * │ 3. User checks absent students to mark present                         │
 * │ 4. User taps Save → recordAttendance() called for each student         │
 * │ 5. todayCheckIns updates → UI refreshes to show new status             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param navController Navigation controller for screen transitions
 * @param sharedViewModels Shared ViewModels containing all app state
 * @param onMenuClick Callback to open the navigation drawer
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceReadinessState
import com.markjayson545.mjdc_applicationcompose.frontend.InputBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.BlockerCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DateFilterBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DropdownSelector
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SetupProgressCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.WarningBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator.DrawerDestinations
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyExitTransition
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.popInTransition
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAttendanceScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Get ViewModels
    val teacherViewModel = sharedViewModels.teacherViewModel
    val studentViewModel = sharedViewModels.studentViewModel
    val subjectViewModel = sharedViewModels.subjectViewModel
    val courseViewModel = sharedViewModels.courseViewModel
    val attendanceViewModel = sharedViewModels.attendanceViewModel

    // Collect states
    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val subjects by subjectViewModel.teacherSubjects.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val todayCheckIns by attendanceViewModel.todayCheckIns.collectAsState()

    // Animation state
    var showContent by remember { mutableStateOf(false) }
    var dismissedWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Load data when teacher is available
    LaunchedEffect(currentTeacher) {
        currentTeacher?.let { teacher ->
            studentViewModel.loadStudentsByTeacher(teacher.teacherId)
            subjectViewModel.loadSubjectsByTeacher(teacher.teacherId)
            courseViewModel.loadCoursesByTeacher(teacher.teacherId)
            attendanceViewModel.loadTodayCheckIns(teacher.teacherId)
        }
    }

    // Calculate readiness state for validation
    val readinessState = remember(students.size, subjects.size, courses.size) {
        AttendanceReadinessState.fromCounts(
            studentCount = students.size,
            subjectCount = subjects.size,
            courseCount = courses.size
        )
    }

    // Date state
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(dateFormat.format(calendar.time)) }

    // Tab state
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Present", "Absent")

    // Bottom sheet state
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedStudents by remember { mutableStateOf<Set<String>>(emptySet()) } // Changed to Set of student IDs
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    // Get present student IDs for today
    val presentStudentIds = remember(todayCheckIns) {
        todayCheckIns.map { it.studentId }.toSet()
    }

    // Calculate counts
    val presentCount = presentStudentIds.size
    val absentCount = students.size - presentCount

    // Filter students based on tab
    val displayList = remember(selectedTabIndex, presentStudentIds, students) {
        when (selectedTabIndex) {
            0 -> students // All
            1 -> students.filter { it.studentId in presentStudentIds } // Present
            2 -> students.filter { it.studentId !in presentStudentIds } // Absent
            else -> students
        }
    }

    // Date picker dialog
    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun addCheckIn() {
        val subject = selectedSubject
        val teacher = currentTeacher

        if (selectedStudents.isNotEmpty() && subject != null && teacher != null) {
            // Record attendance for all selected students
            selectedStudents.forEach { studentId ->
                attendanceViewModel.recordAttendance(
                    studentId = studentId,
                    subjectId = subject.subjectId,
                    teacherId = teacher.teacherId,
                    status = AttendanceStatus.PRESENT
                )
            }
            selectedStudents = emptySet()
            selectedSubject = null
            showBottomSheet = false
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Attendance",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$presentCount Present",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$absentCount Absent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Only show FAB if ready to take attendance
            AnimatedVisibility(
                visible = readinessState.isReady && showContent,
                enter = popInTransition(),
                exit = bouncyExitTransition()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        selectedStudents = emptySet()
                        selectedSubject = null
                        showBottomSheet = true
                    },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Check In", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Show setup checklist if not ready
            if (!readinessState.isReady) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Show the first blocker
                        readinessState.blockers.firstOrNull()?.let { blocker ->
                            BlockerCard(
                                icon = when (blocker) {
                                    com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceBlocker.NO_STUDENTS -> Icons.Default.Groups
                                    com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceBlocker.NO_SUBJECTS -> Icons.AutoMirrored.Filled.MenuBook
                                    else -> Icons.Default.EventBusy
                                },
                                title = blocker.title,
                                message = blocker.message,
                                actionLabel = blocker.actionLabel,
                                onActionClick = {
                                    when (blocker) {
                                        com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceBlocker.NO_STUDENTS ->
                                            navController.navigate(DrawerDestinations.MANAGE_STUDENTS.route)

                                        com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceBlocker.NO_SUBJECTS ->
                                            navController.navigate(DrawerDestinations.MANAGE_SUBJECTS.route)

                                        else -> {}
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Show setup progress
                        SetupProgressCard(
                            hasStudents = readinessState.hasStudents,
                            hasSubjects = readinessState.hasSubjects,
                            hasCourses = readinessState.hasCourses,
                            onAddStudents = { navController.navigate(DrawerDestinations.MANAGE_STUDENTS.route) },
                            onAddSubjects = { navController.navigate(DrawerDestinations.MANAGE_SUBJECTS.route) },
                            onAddCourses = { navController.navigate(DrawerDestinations.MANAGE_COURSES.route) }
                        )
                    }
                }
            } else {
                // Show warnings if any (but not blocking)
                if (readinessState.warnings.isNotEmpty() && !dismissedWarning) {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = bouncyEnterTransition(index = 0)
                    ) {
                        WarningBanner(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            title = "Tip",
                            message = readinessState.warnings.first()
                                .let { "${it.message}. ${it.suggestion}" },
                            onDismiss = { dismissedWarning = true }
                        )
                    }
                }

                // Date Filter with animation
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 1)
                ) {
                    DateFilterBar(
                        selectedDate = selectedDate,
                        onDateClick = { showDatePicker() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Tab Row with animation
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 2)
                ) {
                    PrimaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    val count = when (index) {
                                        0 -> students.size
                                        1 -> presentCount
                                        2 -> absentCount
                                        else -> 0
                                    }
                                    Text(
                                        "$title ($count)",
                                        fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = displayList.isEmpty() && showContent,
                    enter = bouncyEnterTransition(),
                    exit = bouncyExitTransition()
                ) {
                    EmptyStateView(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Default.EventBusy,
                        title = when (selectedTabIndex) {
                            1 -> "No Present Students"
                            2 -> "All Students Present!"
                            else -> "No Students Yet"
                        },
                        subtitle = when (selectedTabIndex) {
                            1 -> "No students have checked in yet today"
                            2 -> "Great job! All students are present"
                            else -> "Start by checking in a student"
                        },
                        actionLabel = if (selectedTabIndex == 0 || selectedTabIndex == 2) "Check In Student" else null,
                        onActionClick = if (selectedTabIndex == 0 || selectedTabIndex == 2) {
                            { showBottomSheet = true }
                        } else null
                    )
                }

                AnimatedVisibility(
                    visible = displayList.isNotEmpty() && showContent,
                    enter = bouncyEnterTransition(index = 3)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(displayList) { index, student ->
                            val isPresent = student.studentId in presentStudentIds
                            val checkIn = todayCheckIns.find { it.studentId == student.studentId }

                            AnimatedVisibility(
                                visible = showContent,
                                enter = bouncyEnterTransition(index = index)
                            ) {
                                AttendanceCard(
                                    student = student,
                                    isPresent = isPresent,
                                    checkInTime = checkIn?.checkInTime,
                                    subjectCode = subjects.find { it.subjectId == checkIn?.subjectId }?.subjectCode
                                        ?: "N/A"
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                        }
                    }
                }
            }
        }
    }

    // Bottom sheet for check-in
    if (showBottomSheet) {
        InputBottomSheet(
            isVisible = true,
            title = "Record Check-In",
            onDismiss = { showBottomSheet = false },
            onSaveClick = { addCheckIn() }
        ) {
            // Filter only absent students
            val absentStudents = students.filter { it.studentId !in presentStudentIds }

            if (absentStudents.isEmpty()) {
                Text(
                    "All students have already checked in!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Subject selector
                if (subjects.isNotEmpty()) {
                    DropdownSelector(
                        label = "Select Subject",
                        selectedItem = selectedSubject,
                        items = subjects,
                        onItemSelected = { selectedSubject = it },
                        itemToString = { "${it.subjectCode} - ${it.subjectName}" },
                        placeholder = "Choose a subject"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student selection header with count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select Students",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (selectedStudents.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "${selectedStudents.size} selected",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Checkbox list of absent students
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Fixed height for scrolling
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(absentStudents) { student ->
                        AbsentStudentCheckCard(
                            student = student,
                            isChecked = student.studentId in selectedStudents,
                            onCheckedChange = { isChecked ->
                                selectedStudents = if (isChecked) {
                                    selectedStudents + student.studentId
                                } else {
                                    selectedStudents - student.studentId
                                }
                            }
                        )
                    }
                }

                // Show summary of selected students
                if (selectedStudents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HowToReg,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "${selectedStudents.size} student${if (selectedStudents.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Will be marked as present${selectedSubject?.let { " for ${it.subjectCode}" } ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AbsentStudentCheckCard(
    student: Student,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 4.dp else 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            androidx.compose.material3.Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isChecked)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${student.firstName.first()}${student.lastName.first()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isChecked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = student.studentId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Check indicator icon
            if (isChecked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AttendanceCard(
    student: Student,
    isPresent: Boolean,
    checkInTime: String?,
    subjectCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with status indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.firstName.first()}${student.lastName.first()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isPresent) Color(0xFF4CAF50) else Color(0xFFF44336))
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.firstName} ${student.middleName} ${student.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${student.studentId} • $subjectCode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status and time
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isPresent) Color(0xFF4CAF50).copy(alpha = 0.9f)
                    else Color(0xFFF44336).copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = if (isPresent) "Present" else "Absent",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isPresent && checkInTime != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = checkInTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

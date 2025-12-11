
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
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.main

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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.CheckIns
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.AppPreferences
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.formattedName
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceBlocker
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceReadinessState
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.AttendanceFilterBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.AttendanceFilterState
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.AttendanceSortBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.AttendanceSortOption
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.BlockerCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DateFilterBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DateRangePreset
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EditAttendanceBottomSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.FilterSortRow
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.FullScreenCheckInSheet
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SearchBar
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
import kotlin.collections.get

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

    // Initialize AppPreferences for name formatting
    LaunchedEffect(Unit) {
        AppPreferences.init(context)
    }

    // Get ViewModels
    val teacherViewModel = sharedViewModels.teacherViewModel
    val studentViewModel = sharedViewModels.studentViewModel
    val subjectViewModel = sharedViewModels.subjectViewModel
    val courseViewModel = sharedViewModels.courseViewModel
    val attendanceViewModel = sharedViewModels.attendanceViewModel
    val enrollmentViewModel = sharedViewModels.enrollmentViewModel

    // Collect states
    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val subjects by subjectViewModel.teacherSubjects.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val todayCheckIns by attendanceViewModel.todayCheckIns.collectAsState()
    val enrolledStudentIds by enrollmentViewModel.enrolledStudentIds.collectAsState()

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
    val displayDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(displayDateFormat.format(calendar.time)) }
    var selectedDbDate by remember { mutableStateOf(dbDateFormat.format(calendar.time)) }

    // Reload check-ins when date changes
    LaunchedEffect(selectedDbDate, currentTeacher) {
        currentTeacher?.let { teacher ->
            attendanceViewModel.loadCheckInsByDate(teacher.teacherId, selectedDbDate)
        }
    }


    // Bottom sheet state - using FullScreenCheckInSheet
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    // Filter & Sort state (not persistent - in-memory only)
    var filterState by remember { mutableStateOf(AttendanceFilterState()) }
    var sortOption by remember { mutableStateOf(AttendanceSortOption.TIME_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Edit attendance state
    var showEditSheet by remember { mutableStateOf(false) }
    var selectedCheckInForEdit by remember { mutableStateOf<CheckIns?>(null) }
    var selectedStudentForEdit by remember { mutableStateOf<Student?>(null) }

    // Get check-ins for selected subject (if filtering by subject)
    val relevantCheckIns = remember(todayCheckIns, filterState.selectedSubjectId) {
        if (filterState.selectedSubjectId != null) {
            todayCheckIns.filter { it.subjectId == filterState.selectedSubjectId }
        } else {
            todayCheckIns
        }
    }

    // Get student IDs who have checked in (considering subject filter)
    val checkedInStudentIds = remember(relevantCheckIns) {
        relevantCheckIns.map { it.studentId }.toSet()
    }

    // Get check-ins grouped by status (considering subject filter)
    val checkInsByStatus = remember(relevantCheckIns) {
        relevantCheckIns.groupBy { it.status }
    }

    // Calculate counts for each status
    val presentCount = checkInsByStatus[AttendanceStatus.PRESENT]?.size ?: 0
    val lateCount = checkInsByStatus[AttendanceStatus.LATE]?.size ?: 0
    val excusedCount = checkInsByStatus[AttendanceStatus.EXCUSED]?.size ?: 0
    val absentCount = students.size - checkedInStudentIds.size

    // Filter students based on filter state (uses relevantCheckIns which considers subject filter)
    val displayList = remember(relevantCheckIns, students, filterState, sortOption, checkedInStudentIds, searchQuery) {
        // Apply search filter first
        val searchFiltered = if (searchQuery.isNotBlank()) {
            students.filter { student ->
                student.formattedName().contains(searchQuery, ignoreCase = true) ||
                student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.studentId.contains(searchQuery, ignoreCase = true)
            }
        } else {
            students
        }

        // Apply status filter if set
        val filteredByStatus = if (filterState.selectedStatuses.isNotEmpty()) {
            searchFiltered.filter { student ->
                val checkIn = relevantCheckIns.find { it.studentId == student.studentId }
                if (checkIn != null) {
                    checkIn.status in filterState.selectedStatuses
                } else {
                    // Student is absent - check if ABSENT is in the filter
                    AttendanceStatus.ABSENT in filterState.selectedStatuses
                }
            }
        } else {
            searchFiltered // Show all students when no status filter is set
        }

        // Apply sorting - handle absent students appropriately
        val checkInMap = relevantCheckIns.associateBy { it.studentId }

        when (sortOption) {
            AttendanceSortOption.NAME_ASC -> filteredByStatus.sortedBy { it.formattedName().lowercase() }
            AttendanceSortOption.NAME_DESC -> filteredByStatus.sortedByDescending { it.formattedName().lowercase() }
            AttendanceSortOption.TIME_ASC -> {
                // Students with check-ins sorted by time, then absent students sorted by name
                filteredByStatus.sortedWith(compareBy(
                    { checkInMap[it.studentId]?.checkInTime == null }, // Absent students last
                    { checkInMap[it.studentId]?.checkInTime ?: "99:99:99" }
                ))
            }
            AttendanceSortOption.TIME_DESC -> {
                // Students with check-ins sorted by time desc, then absent students sorted by name
                filteredByStatus.sortedWith(compareBy(
                    { checkInMap[it.studentId]?.checkInTime == null }, // Absent students last
                    { -(checkInMap[it.studentId]?.checkInTime?.replace(":", "")?.toIntOrNull() ?: 0) }
                ))
            }
            AttendanceSortOption.STATUS -> {
                val statusOrder = mapOf(
                    AttendanceStatus.PRESENT to 0,
                    AttendanceStatus.LATE to 1,
                    AttendanceStatus.EXCUSED to 2,
                    AttendanceStatus.ABSENT to 3
                )
                filteredByStatus.sortedWith(compareBy(
                    { statusOrder[checkInMap[it.studentId]?.status] ?: 4 }, // Absent (null) gets 4
                    { it.formattedName().lowercase() } // Secondary sort by name
                ))
            }
        }
    }

    // Calculate active filter count
    val activeFilterCount = remember(filterState) {
        var count = 0
        if (filterState.selectedSubjectId != null) count++
        if (filterState.selectedStatuses.isNotEmpty()) count += filterState.selectedStatuses.size
        if (filterState.dateRange != DateRangePreset.TODAY) count++
        count
    }

    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = displayDateFormat.format(calendar.time)
                selectedDbDate = dbDateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun addCheckIn(selections: Map<String, AttendanceStatus>, subjectId: String) {
        val teacher = currentTeacher

        if (selections.isNotEmpty() && teacher != null) {
            // Record attendance for all selected students with their individual statuses
            selections.forEach { (studentId, status) ->
                attendanceViewModel.recordAttendance(
                    studentId = studentId,
                    subjectId = subjectId,
                    teacherId = teacher.teacherId,
                    status = status
                )
            }
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Present count
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "$presentCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Absent count
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "$absentCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Late count
                            if (lateCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "$lateCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            // Excused count
                            if (excusedCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2196F3))
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "$excusedCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                    AttendanceBlocker.NO_STUDENTS -> Icons.Default.Groups
                                    AttendanceBlocker.NO_SUBJECTS -> Icons.AutoMirrored.Filled.MenuBook
                                    else -> Icons.Default.EventBusy
                                },
                                title = blocker.title,
                                message = blocker.message,
                                actionLabel = blocker.actionLabel,
                                onActionClick = {
                                    when (blocker) {
                                        AttendanceBlocker.NO_STUDENTS ->
                                            navController.navigate(DrawerDestinations.MANAGE_STUDENTS.route)

                                        AttendanceBlocker.NO_SUBJECTS ->
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

                // Search Bar
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 2)
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search students...",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Filter and Sort Row
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 3)
                ) {
                    FilterSortRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        subjects = subjects,
                        selectedSubject = subjects.find { it.subjectId == filterState.selectedSubjectId },
                        onSubjectSelected = { subject ->
                            filterState = filterState.copy(selectedSubjectId = subject?.subjectId)
                        },
                        hasActiveFilters = activeFilterCount > 0,
                        activeFilterCount = activeFilterCount,
                        onFilterClick = { showFilterSheet = true },
                        sortLabel = sortOption.label,
                        onSortClick = { showSortSheet = true }
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = displayList.isEmpty() && showContent,
                    enter = bouncyEnterTransition(),
                    exit = bouncyExitTransition()
                ) {
                    val hasFilters = filterState.selectedStatuses.isNotEmpty() || filterState.selectedSubjectId != null
                    EmptyStateView(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Default.EventBusy,
                        title = if (hasFilters) "No Matching Students" else "No Students Yet",
                        subtitle = if (hasFilters)
                            "No students match the current filter. Try changing your filters."
                        else
                            "Start by checking in a student",
                        actionLabel = if (!hasFilters) "Check In Student" else "Clear Filters",
                        onActionClick = if (!hasFilters) {
                            { showBottomSheet = true }
                        } else {
                            { filterState = AttendanceFilterState() }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = displayList.isNotEmpty() && showContent,
                    enter = bouncyEnterTransition(index = 4)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(displayList) { index, student ->
                            // Find check-in from relevantCheckIns (subject-filtered if subject is selected)
                            val checkIn = relevantCheckIns.find { it.studentId == student.studentId }
                            val status = checkIn?.status

                            AnimatedVisibility(
                                visible = showContent,
                                enter = bouncyEnterTransition(index = index)
                            ) {
                                AttendanceCard(
                                    student = student,
                                    status = status,
                                    checkInTime = checkIn?.checkInTime,
                                    subjectCode = subjects.find { it.subjectId == checkIn?.subjectId }?.subjectCode
                                        ?: "N/A",
                                    onClick = if (checkIn != null) {
                                        {
                                            selectedCheckInForEdit = checkIn
                                            selectedStudentForEdit = student
                                            showEditSheet = true
                                        }
                                    } else null
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

    // Load enrolled students when a subject is selected
    LaunchedEffect(selectedSubject) {
        selectedSubject?.let { subject ->
            enrollmentViewModel.loadEnrolledStudentsForSubject(subject.subjectId)
        }
    }

    // Compute students who haven't checked in for the selected subject on this date
    // Only show students enrolled in the selected subject
    val studentsForCheckIn = remember(students, todayCheckIns, selectedSubject, enrolledStudentIds) {
        val subject = selectedSubject
        if (subject != null) {
            // Filter to enrolled students not checked in for this specific subject
            val checkedInForSubject = todayCheckIns
                .filter { it.subjectId == subject.subjectId }
                .map { it.studentId }
                .toSet()
            // Only show students who are enrolled in this subject
            students.filter { student ->
                student.studentId in enrolledStudentIds && student.studentId !in checkedInForSubject
            }
        } else {
            // No subject selected yet - show all students
            students
        }
    }

    // Full-screen check-in bottom sheet
    FullScreenCheckInSheet(
        isVisible = showBottomSheet,
        subjects = subjects,
        students = studentsForCheckIn,
        selectedSubject = selectedSubject,
        onSubjectSelected = { subject ->
            selectedSubject = subject
            // Load enrolled students for the new subject
            subject?.let { enrollmentViewModel.loadEnrolledStudentsForSubject(it.subjectId) }
        },
        onDismiss = {
            showBottomSheet = false
            enrollmentViewModel.clearEnrolledStudentIds()
        },
        onSave = { selections, subjectId ->
            addCheckIn(selections, subjectId)
        }
    )

    // Filter bottom sheet
    AttendanceFilterBottomSheet(
        isVisible = showFilterSheet,
        subjects = subjects,
        currentFilter = filterState,
        onFilterChanged = { filterState = it },
        onDismiss = { showFilterSheet = false },
        onClearFilters = {
            filterState = AttendanceFilterState()
        }
    )

    // Sort bottom sheet
    AttendanceSortBottomSheet(
        isVisible = showSortSheet,
        currentSort = sortOption,
        onSortSelected = { sortOption = it },
        onDismiss = { showSortSheet = false }
    )

    // Edit attendance bottom sheet
    if (selectedCheckInForEdit != null && selectedStudentForEdit != null) {
        EditAttendanceBottomSheet(
            isVisible = showEditSheet,
            studentName = selectedStudentForEdit!!.fullName,
            currentStatus = selectedCheckInForEdit!!.status,
            subjectCode = subjects.find { it.subjectId == selectedCheckInForEdit!!.subjectId }?.subjectCode ?: "N/A",
            checkInTime = selectedCheckInForEdit!!.checkInTime,
            onStatusChange = { newStatus ->
                selectedCheckInForEdit?.let { checkIn ->
                    attendanceViewModel.updateAttendance(
                        checkIn.copy(status = newStatus),
                        selectedDbDate
                    )
                }
                showEditSheet = false
                selectedCheckInForEdit = null
                selectedStudentForEdit = null
            },
            onRemoveCheckIn = {
                selectedCheckInForEdit?.let { checkIn ->
                    attendanceViewModel.deleteAttendance(checkIn, selectedDbDate)
                }
                showEditSheet = false
                selectedCheckInForEdit = null
                selectedStudentForEdit = null
            },
            onDismiss = {
                showEditSheet = false
                selectedCheckInForEdit = null
                selectedStudentForEdit = null
            }
        )
    }
}


@Composable
private fun AttendanceCard(
    student: Student,
    status: AttendanceStatus?,
    checkInTime: String?,
    subjectCode: String,
    onClick: (() -> Unit)? = null
) {
    val statusColor = when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF4CAF50)
        AttendanceStatus.ABSENT -> Color(0xFFF44336)
        AttendanceStatus.LATE -> Color(0xFFFF9800)
        AttendanceStatus.EXCUSED -> Color(0xFF2196F3)
        null -> Color(0xFFF44336) // No check-in = absent
    }

    val statusLabel = when (status) {
        AttendanceStatus.PRESENT -> "Present"
        AttendanceStatus.ABSENT -> "Absent"
        AttendanceStatus.LATE -> "Late"
        AttendanceStatus.EXCUSED -> "Excused"
        null -> "Absent"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick ?: {}
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
                        .background(statusColor)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.formattedName(),
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
                    color = statusColor.copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (status != null && checkInTime != null) {
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

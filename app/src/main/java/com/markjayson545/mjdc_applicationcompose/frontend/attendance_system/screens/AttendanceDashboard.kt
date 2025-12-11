/**
 * ============================================================================
 * ATTENDANCE DASHBOARD SCREEN
 * ============================================================================
 *
 * This is the main dashboard/home screen for the attendance system. It provides
 * teachers with an overview of their data and quick access to key features.
 *
 * SCREEN RESPONSIBILITIES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Display teacher-specific statistics (students, courses, subjects)   │
 * │ 2. Show today's attendance rate and recent check-ins                   │
 * │ 3. Guide new users through setup process                               │
 * │ 4. Provide quick navigation to other screens                           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * DATA FLOW:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SharedViewModels                                                       │
 * │       │                                                                 │
 * │       ├── teacherViewModel.currentTeacher → Teacher info for header    │
 * │       ├── studentViewModel.teacherStudents → Student count             │
 * │       ├── courseViewModel.teacherCourses → Course count                │
 * │       ├── subjectViewModel.teacherSubjects → Subject count             │
 * │       └── attendanceViewModel.todayCheckIns → Recent activity list     │
 * │                                                                         │
 * │  LaunchedEffect(currentTeacher) triggers data loading when logged in   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * UI SECTIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Setup Progress Card (shown if !readinessState.isReady)              │
 * │    - Guides users to add students, subjects, courses                   │
 * │                                                                         │
 * │ 2. Warning Banner (shown if ready but has optimization suggestions)    │
 * │                                                                         │
 * │ 3. Overview Stats Grid (2x2 compact stat cards)                        │
 * │    - Students count, Courses count                                     │
 * │    - Today's attendance rate, Subjects count                           │
 * │                                                                         │
 * │ 4. Quick Actions (optional, currently commented out)                   │
 * │                                                                         │
 * │ 5. Recent Check-ins List (today's attendance activity)                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ANIMATIONS:
 * - Uses Material 3 Expressive motion scheme for bouncy animations
 * - Staggered entrance animations for list items
 * - Smooth transitions via bouncyEnterTransition/bouncyExitTransition
 *
 * @param navController Navigation controller for screen transitions
 * @param sharedViewModels Shared ViewModels containing all app state
 * @param onMenuClick Callback to open the navigation drawer
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.bridge.repository.AttendanceReadinessState
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactActivityItem
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactStatsCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.InfoBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SetupProgressCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.WarningBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator.DrawerDestinations
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyExitTransition
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AttendanceDashboardScreen(
    navController: NavController, sharedViewModels: SharedViewModels, onMenuClick: () -> Unit = {}
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Get data from ViewModels
    val teacherViewModel = sharedViewModels.teacherViewModel
    val studentViewModel = sharedViewModels.studentViewModel
    val courseViewModel = sharedViewModels.courseViewModel
    val subjectViewModel = sharedViewModels.subjectViewModel
    val attendanceViewModel = sharedViewModels.attendanceViewModel

    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val subjects by subjectViewModel.teacherSubjects.collectAsState()
    val todayCheckIns by attendanceViewModel.todayCheckIns.collectAsState()

    var showContent by remember { mutableStateOf(false) }
    var dismissedSetupCard by remember { mutableStateOf(false) }

    // Load data when teacher is available
    LaunchedEffect(currentTeacher) {
        currentTeacher?.let { teacher ->
            studentViewModel.loadStudentsByTeacher(teacher.teacherId)
            courseViewModel.loadCoursesByTeacher(teacher.teacherId)
            subjectViewModel.loadSubjectsByTeacher(teacher.teacherId)
            attendanceViewModel.loadTodayCheckIns(teacher.teacherId)
            attendanceViewModel.loadCheckInsByTeacher(teacher.teacherId)
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    val totalStudents = students.size
    val totalCourses = courses.size
    val totalSubjects = subjects.size
    val todayAttendanceCount = todayCheckIns.size

    // Calculate readiness state for showing setup guidance
    val readinessState = remember(totalStudents, totalSubjects, totalCourses) {
        AttendanceReadinessState.fromCounts(
            studentCount = totalStudents, subjectCount = totalSubjects, courseCount = totalCourses
        )
    }

    // Calculate attendance rate
    val attendanceRate = if (totalStudents > 0) {
        (todayAttendanceCount.toFloat() / totalStudents * 100).toInt()
    } else 0

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            "Welcome, ${currentTeacher?.firstName ?: "Teacher"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            currentDate,
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
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Setup guidance for new users
            if (!readinessState.isReady && !dismissedSetupCard) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = bouncyEnterTransition(),
                        exit = bouncyExitTransition()
                    ) {
                        SetupProgressCard(
                            hasStudents = readinessState.hasStudents,
                            hasSubjects = readinessState.hasSubjects,
                            hasCourses = readinessState.hasCourses,
                            onAddStudents = { navController.navigate(DrawerDestinations.MANAGE_STUDENTS.route) },
                            onAddSubjects = { navController.navigate(DrawerDestinations.MANAGE_SUBJECTS.route) },
                            onAddCourses = { navController.navigate(DrawerDestinations.MANAGE_COURSES.route) })
                    }
                }
            }

            // Show warning if ready but has warnings
            if (readinessState.isReady && readinessState.warnings.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = bouncyEnterTransition(),
                        exit = bouncyExitTransition()
                    ) {
                        WarningBanner(
                            title = "Tip",
                            message = readinessState.warnings.first()
                                .let { "${it.message}. ${it.suggestion}" })
                    }
                }
            }

            // Stats Section
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec())
                ) {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 0)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Students",
                            value = totalStudents,
                            icon = Icons.Default.Groups,
                            iconTint = Color(0xFF2196F3)
                        )
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Courses",
                            value = totalCourses,
                            icon = Icons.Default.Book,
                            iconTint = Color(0xFF9C27B0)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 1)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Today's Rate",
                            value = attendanceRate,
                            suffix = "%",
                            showValue = totalStudents > 0,
                            icon = Icons.Default.CheckCircle,
                            iconTint = when {
                                attendanceRate >= 80 -> Color(0xFF4CAF50)
                                attendanceRate >= 50 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Subjects",
                            value = totalSubjects,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            iconTint = Color(0xFFFF9800)
                        )
                    }
                }
            }

            // Quick Actions Section
//            item {
//                AnimatedVisibility(
//                    visible = showContent,
//                    enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec())
//                ) {
//                    Column {
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            "Quick Actions",
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.SemiBold,
//                            modifier = Modifier.padding(bottom = 4.dp)
//                        )
//                    }
//                }
//            }

//        item {
//            AnimatedVisibility(
//                visible = showContent,
//                enter = bouncyEnterTransition(index = 2)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    QuickActionButton(
//                        modifier = Modifier.weight(1f),
//                        title = "Mark\nAttendance",
//                        icon = Icons.Default.CalendarMonth,
//                        iconTint = if (readinessState.isReady) Color(0xFF4CAF50) else Color.Gray,
//                        onClick = {
//                            if (readinessState.isReady) {
//                                navController.navigate(AttendanceDestinations.MANAGE_ATTENDANCE.route)
//                            }
//                        })
//                    QuickActionButton(
//                        modifier = Modifier.weight(1f),
//                        title = "View\nReports",
//                        icon = Icons.Default.Assessment,
//                        iconTint = Color(0xFF2196F3),
//                        onClick = { navController.navigate(DrawerDestinations.REPORTS.route) })
//                    QuickActionButton(
//                        modifier = Modifier.weight(1f),
//                        title = "Add\nStudent",
//                        icon = Icons.Default.PersonAdd,
//                        iconTint = Color(0xFF9C27B0),
//                        onClick = { navController.navigate(AttendanceDestinations.MANAGE_STUDENTS.route) })
//                    QuickActionButton(
//                        modifier = Modifier.weight(1f),
//                        title = "Add\nCourse",
//                        icon = Icons.Default.PostAdd,
//                        iconTint = Color(0xFFFF9800),
//                        onClick = { navController.navigate(AttendanceDestinations.MANAGE_COURSES.route) })
//                }
//            }
//        }

            // Recent Activity Section
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec())
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Recent Check-ins",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (todayCheckIns.isNotEmpty()) {
                                Text(
                                    "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            if (todayCheckIns.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = bouncyEnterTransition()
                    ) {
                        InfoBanner(
                            title = "No Check-ins Today",
                            message = if (readinessState.isReady) "Start taking attendance to see check-ins here"
                            else "Complete the setup above to start taking attendance"
                        )
                    }
                }
            } else {
                itemsIndexed(todayCheckIns.take(5)) { index, checkIn ->
                    val student = students.find { it.studentId == checkIn.studentId }
                    val subject = subjects.find { it.subjectId == checkIn.subjectId }

                    AnimatedVisibility(
                        visible = showContent,
                        enter = bouncyEnterTransition(index = index)
                    ) {
                        CompactActivityItem(
                            studentName = student?.fullName ?: "Unknown Student",
                            subjectCode = subject?.subjectCode ?: "N/A",
                            time = checkIn.checkInTime,
                            isPresent = checkIn.status == AttendanceStatus.PRESENT
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

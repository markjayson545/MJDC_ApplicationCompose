/**
 * ============================================================================
 * REPORTS SCREEN
 * ============================================================================
 *
 * This screen displays attendance reports and analytics for the current
 * teacher, providing insights into attendance patterns and statistics.
 *
 * SCREEN RESPONSIBILITIES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Display overall attendance rate with visual progress indicator      │
 * │ 2. Show total student count and today's check-ins                      │
 * │ 3. Break down attendance by status (present, absent, late, excused)    │
 * │ 4. Provide summary text with key metrics                               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * DATA FLOW:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SharedViewModels                                                       │
 * │       │                                                                 │
 * │       ├── teacherViewModel.currentTeacher → Teacher context            │
 * │       ├── studentViewModel.teacherStudents → Total student count       │
 * │       └── attendanceViewModel                                           │
 * │            ├── teacherCheckIns → All attendance records                │
 * │            └── todayCheckIns → Today's check-ins only                  │
 * │                                                                         │
 * │  STATISTICS CALCULATIONS:                                               │
 * │  - totalStudents: students.size                                        │
 * │  - totalCheckIns: allCheckIns.size                                     │
 * │  - todayCount: todayCheckIns.size                                      │
 * │  - presentCount: allCheckIns.count { status == PRESENT }               │
 * │  - attendanceRate: (presentCount / totalCheckIns * 100)                │
 * │                                                                         │
 * │  REPOSITORY METHODS USED:                                               │
 * │  - AttendanceRepository.getCheckInsByTeacher() for all records         │
 * │  - AttendanceRepository.getTodayCheckIns() for today's data            │
 * │  - AttendanceRepository.getAttendanceStats() (could be integrated)     │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * UI SECTIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Large Top App Bar - Title with current date                         │
 * │ 2. Attendance Rate Card - Percentage with progress bar                 │
 * │ 3. Stats Grid - Total students, today's check-ins                      │
 * │ 4. Breakdown Grid - Present, absent, late, excused counts              │
 * │ 5. Summary Card - Text summary of all statistics                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * VISUAL INDICATORS:
 * - Attendance rate color: Green (≥80%), Orange (≥60%), Red (<60%)
 * - Progress bar reflects attendance percentage
 * - Color-coded stat cards for each status type
 *
 * @param navController Navigation controller for screen transitions
 * @param sharedViewModels Shared ViewModels containing all app state
 * @param onMenuClick Callback to open the navigation drawer
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactStatsCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.InfoBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {
    // Get ViewModels
    val teacherViewModel = sharedViewModels.teacherViewModel
    val studentViewModel = sharedViewModels.studentViewModel
    val attendanceViewModel = sharedViewModels.attendanceViewModel

    // Collect states
    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val students by studentViewModel.teacherStudents.collectAsState()
    val allCheckIns by attendanceViewModel.teacherCheckIns.collectAsState()
    val todayCheckIns by attendanceViewModel.todayCheckIns.collectAsState()

    // Animation state
    var showContent by remember { mutableStateOf(false) }

    // Trigger animation
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Load data when teacher is available
    LaunchedEffect(currentTeacher) {
        currentTeacher?.let { teacher ->
            studentViewModel.loadStudentsByTeacher(teacher.teacherId)
            attendanceViewModel.loadCheckInsByTeacher(teacher.teacherId)
            attendanceViewModel.loadTodayCheckIns(teacher.teacherId)
        }
    }

    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    // Calculate statistics
    val totalStudents = students.size
    val totalCheckIns = allCheckIns.size
    val todayCount = todayCheckIns.size
    val presentCount = allCheckIns.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = allCheckIns.count { it.status == AttendanceStatus.ABSENT }
    val lateCount = allCheckIns.count { it.status == AttendanceStatus.LATE }
    val excusedCount = allCheckIns.count { it.status == AttendanceStatus.EXCUSED }

    val attendanceRate = if (totalCheckIns > 0) {
        (presentCount.toFloat() / totalCheckIns * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Reports",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info banner if no data
            item {
                AnimatedVisibility(
                    visible = totalCheckIns == 0 && showContent,
                    enter = bouncyEnterTransition(fromBottom = false)
                ) {
                    InfoBanner(
                        title = "No Data Yet",
                        message = "Start taking attendance to see reports and analytics here."
                    )
                }
            }

            // Attendance Rate Card
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 0)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Attendance Rate",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Based on all records",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "$attendanceRate%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        attendanceRate >= 80 -> Color(0xFF4CAF50)
                                        attendanceRate >= 60 -> Color(0xFFFF9800)
                                        else -> Color(0xFFF44336)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LinearProgressIndicator(
                                progress = { attendanceRate / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = when {
                                    attendanceRate >= 80 -> Color(0xFF4CAF50)
                                    attendanceRate >= 60 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Stats Grid
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 1)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            title = "Today",
                            value = todayCount,
                            icon = Icons.Default.Schedule,
                            iconTint = Color(0xFF9C27B0)
                        )
                    }
                }
            }

            // Attendance Breakdown Section
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 2)
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 3)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Present",
                            value = presentCount,
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF4CAF50)
                        )
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Absent",
                            value = absentCount,
                            icon = Icons.Default.Cancel,
                            iconTint = Color(0xFFF44336)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 4)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Late",
                            value = lateCount,
                            icon = Icons.Default.Schedule,
                            iconTint = Color(0xFFFF9800)
                        )
                        CompactStatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Excused",
                            value = excusedCount,
                            icon = Icons.Default.Warning,
                            iconTint = Color(0xFF607D8B)
                        )
                    }
                }
            }

            // Summary Card
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 5)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Text(
                                    "Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = buildString {
                                    append("You have $totalStudents students in your classes. ")
                                    append("Today, $todayCount students have checked in. ")
                                    if (totalCheckIns > 0) {
                                        append("Overall, your attendance rate is $attendanceRate%, ")
                                        append("with $presentCount present, $absentCount absent, ")
                                        append("$lateCount late, and $excusedCount excused records.")
                                    } else {
                                        append("No attendance records have been recorded yet.")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}



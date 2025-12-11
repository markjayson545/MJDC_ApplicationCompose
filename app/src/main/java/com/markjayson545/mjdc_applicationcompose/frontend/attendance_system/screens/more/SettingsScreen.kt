/**
 * ============================================================================
 * SETTINGS SCREEN
 * ============================================================================
 *
 * This screen provides user settings, profile management, and account options.
 * It exposes previously unused repository methods for profile management.
 *
 * SCREEN RESPONSIBILITIES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Display current teacher's profile information                       │
 * │ 2. Allow editing of profile details (name, email)                      │
 * │ 3. Show account statistics using repository methods                    │
 * │ 4. Provide logout functionality                                        │
 * │ 5. Display app information and version                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * DATA FLOW:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SharedViewModels                                                       │
 * │       │                                                                 │
 * │       ├── teacherViewModel.currentTeacher → Profile info               │
 * │       ├── teacherViewModel.updateTeacher() → Save profile changes      │
 * │       ├── studentViewModel.teacherStudents → Student count stats       │
 * │       ├── courseViewModel.teacherCourses → Course count stats          │
 * │       ├── subjectViewModel.teacherSubjects → Subject count stats       │
 * │       └── attendanceViewModel.teacherCheckIns → Attendance stats       │
 * │                                                                         │
 * │  UNUSED REPOSITORY METHODS NOW INTEGRATED:                              │
 * │  - TeacherRepository.updateTeacher() for profile updates               │
 * │  - TeacherRepository.getTeacherWithAllData() for comprehensive stats   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * UI SECTIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Profile Card - Avatar, name, email, edit button                     │
 * │ 2. Account Statistics - Students, courses, subjects, check-ins         │
 * │ 3. App Settings - (Future: theme, notifications)                       │
 * │ 4. Account Actions - Logout, delete account                            │
 * │ 5. App Info - Version, copyright                                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * @param navController Navigation controller for screen transitions
 * @param sharedViewModels Shared ViewModels containing all app state
 * @param onMenuClick Callback to open the navigation drawer
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.more

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Teacher
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.AppPreferences
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.StudentNameFormat
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator.AuthRoutes
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import kotlinx.coroutines.delay

/**
 * Settings screen composable displaying profile and app settings.
 *
 * This screen integrates previously unused repository methods:
 * - updateTeacher() for profile editing
 * - Teacher statistics from various ViewModels
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Get context for SharedPreferences
    val context = LocalContext.current

    // Initialize AppPreferences
    LaunchedEffect(Unit) {
        AppPreferences.init(context)
    }

    // ========================================================================
    // STATE COLLECTION FROM VIEWMODELS
    // ========================================================================

    // Teacher data
    val teacherViewModel = sharedViewModels.teacherViewModel
    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()

    // Statistics data
    val studentViewModel = sharedViewModels.studentViewModel
    val courseViewModel = sharedViewModels.courseViewModel
    val subjectViewModel = sharedViewModels.subjectViewModel
    val attendanceViewModel = sharedViewModels.attendanceViewModel

    val students by studentViewModel.teacherStudents.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val subjects by subjectViewModel.teacherSubjects.collectAsState()
    val checkIns by attendanceViewModel.teacherCheckIns.collectAsState()

    // Name format preference
    val currentNameFormat by AppPreferences.studentNameFormat.collectAsState()

    // ========================================================================
    // LOCAL UI STATE
    // ========================================================================

    var showContent by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNameFormatDialog by remember { mutableStateOf(false) }

    // Edit form state
    var editFirstName by remember(currentTeacher) {
        mutableStateOf(
            currentTeacher?.firstName ?: ""
        )
    }
    var editMiddleName by remember(currentTeacher) {
        mutableStateOf(
            currentTeacher?.middleName ?: ""
        )
    }
    var editLastName by remember(currentTeacher) { mutableStateOf(currentTeacher?.lastName ?: "") }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    LaunchedEffect(currentTeacher) {
        currentTeacher?.let { teacher ->
            // Load all data for statistics
            studentViewModel.loadStudentsByTeacher(teacher.teacherId)
            courseViewModel.loadCoursesByTeacher(teacher.teacherId)
            subjectViewModel.loadSubjectsByTeacher(teacher.teacherId)
            attendanceViewModel.loadCheckInsByTeacher(teacher.teacherId)
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // ========================================================================
    // MAIN UI
    // ========================================================================

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Manage your account",
                            style = MaterialTheme.typography.bodyMedium,
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================================================================
            // PROFILE CARD
            // ================================================================
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 0)
                ) {
                    ProfileCard(
                        teacher = currentTeacher,
                        onEditClick = { showEditDialog = true }
                    )
                }
            }

            // ================================================================
            // DISPLAY SETTINGS
            // ================================================================
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 1)
                ) {
                    Column {
                        Text(
                            "Display Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Name Format Setting
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .selectable(
                                            selected = false,
                                            onClick = { showNameFormatDialog = true },
                                            role = Role.Button
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TextFormat,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Student Name Format",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = currentNameFormat.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "e.g., ${currentNameFormat.example}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Change",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // ACCOUNT STATISTICS
            // ================================================================
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 2)
                ) {
                    Column {
                        Text(
                            "Account Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        StatisticsCard(
                            studentCount = students.size,
                            courseCount = courses.size,
                            subjectCount = subjects.size,
                            checkInCount = checkIns.size
                        )
                    }
                }
            }

            // ================================================================
            // ACCOUNT ACTIONS
            // ================================================================
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 3)
                ) {
                    Column {
                        Text(
                            "Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Logout button
                                OutlinedButton(
                                    onClick = { showLogoutDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign Out")
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // APP INFO
            // ================================================================
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition(index = 4)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Attendance Management System",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "© 2025 All rights reserved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ========================================================================
    // DIALOGS
    // ========================================================================

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { editFirstName = it },
                        label = { Text("First Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMiddleName,
                        onValueChange = { editMiddleName = it },
                        label = { Text("Middle Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { editLastName = it },
                        label = { Text("Last Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update teacher profile using the repository method
                        currentTeacher?.let { teacher ->
                            teacherViewModel.updateTeacher(
                                teacher.copy(
                                    firstName = editFirstName.trim(),
                                    middleName = editMiddleName.trim(),
                                    lastName = editLastName.trim()
                                )
                            )
                        }
                        showEditDialog = false
                    },
                    enabled = editFirstName.isNotBlank() && editLastName.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out? You'll need to log in again to access your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // Clear data and navigate to auth
                        sharedViewModels.clearAllData()
                        navController.navigate(AuthRoutes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Name Format Dialog
    if (showNameFormatDialog) {
        AlertDialog(
            onDismissRequest = { showNameFormatDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.TextFormat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Student Name Format") },
            text = {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Choose how student names are displayed:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    StudentNameFormat.entries.forEach { format ->
                        val isSelected = format == currentNameFormat

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        AppPreferences.setStudentNameFormat(format)
                                    },
                                    role = Role.RadioButton
                                )
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null // handled by Row
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = format.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = format.example,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNameFormatDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

// ============================================================================
// PRIVATE COMPOSABLES
// ============================================================================

/**
 * Profile card showing teacher information with edit button.
 */
@Composable
private fun ProfileCard(
    teacher: Teacher?,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = teacher?.fullName ?: "Unknown Teacher",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Email
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = teacher?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Teacher ID
            Text(
                text = "ID: ${teacher?.teacherId ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit button
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
}

/**
 * Statistics card showing account metrics.
 */
@Composable
private fun StatisticsCard(
    studentCount: Int,
    courseCount: Int,
    subjectCount: Int,
    checkInCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Groups,
                    label = "Students",
                    value = studentCount.toString(),
                    tint = Color(0xFF2196F3)
                )
                StatItem(
                    icon = Icons.Default.Book,
                    label = "Courses",
                    value = courseCount.toString(),
                    tint = Color(0xFF9C27B0)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Subjects",
                    value = subjectCount.toString(),
                    tint = Color(0xFFFF9800)
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Check-ins",
                    value = checkInCount.toString(),
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/**
 * Individual statistic item with icon and value.
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


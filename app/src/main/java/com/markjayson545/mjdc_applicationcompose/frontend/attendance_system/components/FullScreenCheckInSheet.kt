/**
 * ============================================================================
 * FULL SCREEN CHECK-IN BOTTOM SHEET
 * ============================================================================
 *
 * A full-screen modal bottom sheet for recording student attendance.
 * This replaces the standard InputBottomSheet to fix UX issues with
 * student list scrolling.
 *
 * KEY FEATURES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ - Full screen expansion (skipPartiallyExpanded = true)                  │
 * │ - Proper scrollable student list                                        │
 * │ - 4 attendance status options (Present, Absent, Late, Excused)          │
 * │ - Subject selection dropdown                                            │
 * │ - Multi-select student checkboxes                                       │
 * │ - Status selection per student or batch                                 │
 * │ - Summary card showing selection preview                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * USAGE:
 * Replace InputBottomSheet usage in ManageAttendance.kt with this component.
 *
 * @see ManageAttendanceScreen
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.formattedName
import kotlinx.coroutines.launch

/**
 * Data class representing a student selection with their attendance status
 */
data class StudentCheckInSelection(
    val studentId: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT
)

/**
 * Full Screen Check-In Bottom Sheet
 *
 * A modal bottom sheet that expands to full screen for better UX when
 * recording attendance for multiple students.
 *
 * @param isVisible Whether the sheet is visible
 * @param subjects List of available subjects
 * @param students List of students to show (typically absent students)
 * @param selectedSubject Currently selected subject
 * @param onSubjectSelected Callback when subject is selected
 * @param onDismiss Callback when sheet is dismissed
 * @param onSave Callback when save is clicked with selected students and their statuses
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenCheckInSheet(
    isVisible: Boolean,
    subjects: List<Subject>,
    students: List<Student>,
    selectedSubject: Subject?,
    onSubjectSelected: (Subject?) -> Unit,
    onDismiss: () -> Unit,
    onSave: (selections: Map<String, AttendanceStatus>, subjectId: String) -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Track selected students with their statuses
    var selections by remember { mutableStateOf<Map<String, AttendanceStatus>>(emptyMap()) }

    // Default status for new selections
    var defaultStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Clear selections and search when subject changes (students list changes)
    LaunchedEffect(selectedSubject) {
        selections = emptyMap()
        searchQuery = ""
    }

    // Filter students based on search query
    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) {
            students
        } else {
            students.filter { student ->
                student.firstName.contains(searchQuery, ignoreCase = true) ||
                student.lastName.contains(searchQuery, ignoreCase = true) ||
                student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.studentId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null // We'll use custom header
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            topBar = {
                // Custom header with close button and title
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Drag handle indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Record Attendance",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${students.size} students available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Save button and summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .windowInsetsPadding(WindowInsets.ime)
                    ) {
                        // Summary card when students are selected
                        AnimatedVisibility(visible = selections.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
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
                                            "${selections.size} student${if (selections.size > 1) "s" else ""} selected",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )

                                        // Show status breakdown
                                        val statusCounts = selections.values.groupingBy { it }.eachCount()
                                        val breakdown = statusCounts.entries.joinToString(" • ") {
                                            "${it.value} ${getStatusLabel(it.key)}"
                                        }
                                        Text(
                                            text = breakdown + (selectedSubject?.let { " for ${it.subjectCode}" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                selectedSubject?.let { subject ->
                                    if (selections.isNotEmpty()) {
                                        onSave(selections, subject.subjectId)
                                    }
                                }
                            },
                            enabled = selections.isNotEmpty() && selectedSubject != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = if (selections.isEmpty()) "Select Students"
                                       else "Save Attendance (${selections.size})",
                                modifier = Modifier.padding(vertical = 4.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject Selector
                item {
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    DropdownSelector(
                        label = "",
                        selectedItem = selectedSubject,
                        items = subjects,
                        onItemSelected = onSubjectSelected,
                        itemToString = { "${it.subjectCode} - ${it.subjectName}" },
                        placeholder = "Select a subject"
                    )
                }

                // Default Status Selection
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Default Status for Selection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AttendanceStatusSelector(
                        selectedStatus = defaultStatus,
                        onStatusSelected = { defaultStatus = it }
                    )
                }

                // Student List Header with Search
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Students",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Search Bar
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search students...",
                    )
                }

                // Select All / Clear All Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "${filteredStudents.size} results"
                            else
                                "${students.size} students",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Select All button (for filtered students)
                            val allFilteredSelected = filteredStudents.isNotEmpty() &&
                                filteredStudents.all { it.studentId in selections }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (allFilteredSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.primary,
                                onClick = {
                                    if (allFilteredSelected) {
                                        // Deselect all filtered students
                                        selections = selections - filteredStudents.map { it.studentId }.toSet()
                                    } else {
                                        // Select all filtered students with default status
                                        val newSelections = filteredStudents.associate {
                                            it.studentId to defaultStatus
                                        }
                                        selections = selections + newSelections
                                    }
                                }
                            ) {
                                Text(
                                    text = if (allFilteredSelected) "Deselect All" else "Select All",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (allFilteredSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            // Clear All button (only show if any selections)
                            if (selections.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    onClick = { selections = emptyMap() }
                                ) {
                                    Text(
                                        text = "Clear All",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Empty state
                if (filteredStudents.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = if (searchQuery.isNotBlank())
                                Icons.Default.SearchOff
                            else
                                Icons.Default.EventAvailable,
                            title = if (searchQuery.isNotBlank())
                                "No Students Found"
                            else
                                "All Students Checked In",
                            subtitle = if (searchQuery.isNotBlank())
                                "No students match \"$searchQuery\""
                            else
                                "All students have already been marked for today"
                        )
                    }
                }

                // Student List
                items(filteredStudents, key = { it.studentId }) { student ->
                    val isSelected = student.studentId in selections
                    val currentStatus = selections[student.studentId] ?: defaultStatus

                    StudentCheckInCard(
                        student = student,
                        isSelected = isSelected,
                        status = currentStatus,
                        onCheckedChange = { checked ->
                            selections = if (checked) {
                                selections + (student.studentId to defaultStatus)
                            } else {
                                selections - student.studentId
                            }
                        },
                        onStatusChange = { newStatus ->
                            if (isSelected) {
                                selections = selections + (student.studentId to newStatus)
                            }
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Attendance Status Selector - Row of toggle buttons for selecting status
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AttendanceStatusSelector(
    modifier: Modifier = Modifier,
    selectedStatus: AttendanceStatus,
    onStatusSelected: (AttendanceStatus) -> Unit,
    showAllOptions: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val statusOptions = if (showAllOptions) {
            AttendanceStatus.entries
        } else {
            listOf(AttendanceStatus.PRESENT, AttendanceStatus.ABSENT, AttendanceStatus.LATE, AttendanceStatus.EXCUSED)
        }

        statusOptions.forEach { status ->
            val isSelected = status == selectedStatus
            val statusColor = getStatusColor(status)
            val icon = getStatusIcon(status)

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "statusScale"
            )

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) statusColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                label = "statusBg"
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale),
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) statusColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                onClick = { onStatusSelected(status) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getStatusLabel(status),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Student Check-In Card with status selection
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StudentCheckInCard(
    student: Student,
    isSelected: Boolean,
    status: AttendanceStatus,
    onCheckedChange: (Boolean) -> Unit,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    val statusColor = getStatusColor(status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                statusColor.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = { onCheckedChange(!isSelected) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = statusColor,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                statusColor.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.firstName.first()}${student.lastName.first()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Student Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.formattedName(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                    Text(
                        text = student.studentId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status indicator when selected
                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.9f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = getStatusLabel(status),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Status selection chips when selected
            AnimatedVisibility(visible = isSelected) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AttendanceStatus.entries.forEach { statusOption ->
                            val isStatusSelected = status == statusOption
                            val optionColor = getStatusColor(statusOption)

                            FilterChip(
                                selected = isStatusSelected,
                                onClick = { onStatusChange(statusOption) },
                                label = {
                                    Text(
                                        text = getStatusLabel(statusOption),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = optionColor.copy(alpha = 0.2f),
                                    selectedLabelColor = optionColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = optionColor.copy(alpha = 0.3f),
                                    selectedBorderColor = optionColor,
                                    enabled = true,
                                    selected = isStatusSelected
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get status icon
 */
@Composable
fun getStatusIcon(status: AttendanceStatus): ImageVector {
    return when (status) {
        AttendanceStatus.PRESENT -> Icons.Default.CheckCircle
        AttendanceStatus.ABSENT -> Icons.Default.EventBusy
        AttendanceStatus.LATE -> Icons.Default.Schedule
        AttendanceStatus.EXCUSED -> Icons.Default.EventAvailable
    }
}

@Preview(showBackground = true)
@Composable
fun CheckInCardPreview() {
    StudentCheckInCard(
        student = Student(
            studentId = "2021001",
            firstName = "John",
            lastName = "Doe",
            middleName = "Smith",
            courseId = "BSCS"
        ),
        isSelected = true,
        status = AttendanceStatus.LATE,
        onCheckedChange = {},
        onStatusChange = {}
    )
}


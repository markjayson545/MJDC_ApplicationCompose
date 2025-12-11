package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.bridge.preferences.formattedName

/**
 * Bottom sheet for managing a student's subject enrollments.
 * Shows all available subjects with checkboxes for enrollment status.
 *
 * @param isVisible Whether the bottom sheet is visible
 * @param student The student being enrolled
 * @param subjects All available subjects
 * @param enrolledSubjectIds Currently enrolled subject IDs
 * @param onSave Callback with the new list of enrolled subject IDs
 * @param onDismiss Callback when the sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEnrollmentBottomSheet(
    isVisible: Boolean,
    student: Student?,
    subjects: List<Subject>,
    enrolledSubjectIds: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Track selected subject IDs locally
    val selectedSubjectIds = remember { mutableStateListOf<String>() }

    // Initialize with enrolled subjects when sheet opens
    LaunchedEffect(isVisible, enrolledSubjectIds) {
        if (isVisible) {
            selectedSubjectIds.clear()
            selectedSubjectIds.addAll(enrolledSubjectIds)
        }
    }

    if (isVisible && student != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Enroll in Subjects",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student.formattedName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Select All / Deselect All
                    TextButton(
                        onClick = {
                            if (selectedSubjectIds.size == subjects.size) {
                                selectedSubjectIds.clear()
                            } else {
                                selectedSubjectIds.clear()
                                selectedSubjectIds.addAll(subjects.map { it.subjectId })
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (selectedSubjectIds.size == subjects.size) "Deselect All" else "Select All"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject count info
                Text(
                    text = "${selectedSubjectIds.size} of ${subjects.size} subjects selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (subjects.isEmpty()) {
                    EmptyStateView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "No Subjects Available",
                        subtitle = "Create subjects first to enroll students"
                    )
                } else {
                    // Subject list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects, key = { it.subjectId }) { subject ->
                            val isSelected = subject.subjectId in selectedSubjectIds

                            EnrollmentSelectableCard(
                                title = subject.subjectName,
                                subtitle = subject.subjectCode,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedSubjectIds.remove(subject.subjectId)
                                    } else {
                                        selectedSubjectIds.add(subject.subjectId)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onSave(selectedSubjectIds.toList()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Enrollments")
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet for managing a subject's student enrollments.
 * Shows all available students with checkboxes for enrollment status.
 *
 * @param isVisible Whether the bottom sheet is visible
 * @param subject The subject being managed
 * @param students All available students
 * @param enrolledStudentIds Currently enrolled student IDs
 * @param onSave Callback with the new list of enrolled student IDs
 * @param onDismiss Callback when the sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectEnrollmentBottomSheet(
    isVisible: Boolean,
    subject: Subject?,
    students: List<Student>,
    enrolledStudentIds: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Track selected student IDs locally
    val selectedStudentIds = remember { mutableStateListOf<String>() }

    // Initialize with enrolled students when sheet opens
    LaunchedEffect(isVisible, enrolledStudentIds) {
        if (isVisible) {
            selectedStudentIds.clear()
            selectedStudentIds.addAll(enrolledStudentIds)
        }
    }

    if (isVisible && subject != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Enroll Students",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${subject.subjectCode} - ${subject.subjectName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Select All / Deselect All
                    TextButton(
                        onClick = {
                            if (selectedStudentIds.size == students.size) {
                                selectedStudentIds.clear()
                            } else {
                                selectedStudentIds.clear()
                                selectedStudentIds.addAll(students.map { it.studentId })
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (selectedStudentIds.size == students.size) "Deselect All" else "Select All"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Student count info
                Text(
                    text = "${selectedStudentIds.size} of ${students.size} students selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (students.isEmpty()) {
                    EmptyStateView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        icon = Icons.Default.Groups,
                        title = "No Students Available",
                        subtitle = "Add students first to enroll them"
                    )
                } else {
                    // Student list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students, key = { it.studentId }) { student ->
                            val isSelected = student.studentId in selectedStudentIds

                            EnrollmentSelectableCard(
                                title = student.formattedName(),
                                subtitle = student.studentId,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedStudentIds.remove(student.studentId)
                                    } else {
                                        selectedStudentIds.add(student.studentId)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onSave(selectedStudentIds.toList()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Enrollments")
                    }
                }
            }
        }
    }
}

/**
 * A selectable card for enrollment selection lists.
 */
@Composable
fun EnrollmentSelectableCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * A small badge showing enrollment count.
 */
@Composable
fun EnrollmentCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    label: String = "subjects"
) {
    if (count > 0) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = "$count $label",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


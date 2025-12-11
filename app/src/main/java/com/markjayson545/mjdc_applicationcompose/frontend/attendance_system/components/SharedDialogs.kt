package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Student
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.UserAlt
import compose.icons.fontawesomeicons.solid.UserAltSlash

@Composable
fun ConfirmationDialog(
    icon: ImageVector,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    itemType: String,
    additionalMessage: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = buildString {
        append("Are you sure you want to delete \"$itemName\"?")
        if (additionalMessage != null) {
            append(" $additionalMessage")
        }
        append(" This action cannot be undone.")
    }

    ConfirmationDialog(
        icon = Icons.Default.Delete,
        title = "Delete $itemType",
        message = message,
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun InfoDialog(
    title: String,
    message: String,
    confirmText: String = "OK",
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = confirmText)
            }
        }
    )
}

@Composable
fun WarningDialog(
    title: String,
    message: String,
    confirmText: String = "Continue",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        icon = Icons.Default.Warning,
        title = title,
        message = message,
        confirmText = confirmText,
        isDestructive = false,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    title: String,
    student: Student? = null,
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (firstName: String, middleName: String, lastName: String, courseId: String?) -> Unit
) {
    var firstName by remember { mutableStateOf(student?.firstName ?: "") }
    var middleName by remember { mutableStateOf(student?.middleName ?: "") }
    var lastName by remember { mutableStateOf(student?.lastName ?: "") }
    var selectedCourse by remember { mutableStateOf(courses.find { it.courseId == student?.courseId }) }
    var expanded by remember { mutableStateOf(false) }

    FormDialog(
        onDismissRequest = onDismiss,
        onConfirmation = { onConfirm(firstName, middleName, lastName, selectedCourse?.courseId) },
        dialogIcon = if (student == null) Icons.Default.Add else Icons.Default.Edit,
        dialogTitle = title,
        confirmEnabled = firstName.isNotBlank() && lastName.isNotBlank() && selectedCourse != null
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            placeholder = { Text("e.g. John") },
            leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Middle Name") },
            placeholder = { Text("Optional") },
            leadingIcon = { Icon(Icons.Default.TextFields, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            placeholder = { Text("e.g. Doe") },
            leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (courses.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCourse?.courseName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course", overflow = TextOverflow.Ellipsis) },
                    placeholder = { Text("Select a course", overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryEditable, true
                        )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text("${course.courseCode} - ${course.courseName}") },
                            onClick = {
                                selectedCourse = course
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseFormDialog(
    title: String,
    course: Course? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String) -> Unit
) {
    var name by remember { mutableStateOf(course?.courseName ?: "") }
    var code by remember { mutableStateOf(course?.courseCode ?: "") }

    FormDialog(
        onDismissRequest = onDismiss,
        onConfirmation = { onConfirm(name, code) },
        dialogIcon = if (course == null) Icons.Default.Add else Icons.Default.Edit,
        dialogTitle = title,
        confirmEnabled = name.isNotBlank() && code.isNotBlank()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Course Name") },
            placeholder = { Text("e.g. Bachelor of Science in Information Technology") },
            leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Course Code") },
            placeholder = { Text("e.g. BSIT") },
            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectFormDialog(
    title: String,
    subject: Subject? = null,
    courses: List<Course> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, courseIds: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(subject?.subjectName ?: "") }
    var code by remember { mutableStateOf(subject?.subjectCode ?: "") }
    var selectedCourseIds by remember { mutableStateOf<List<String>>(emptyList()) }

    FormDialog(
        onDismissRequest = onDismiss,
        onConfirmation = { onConfirm(name, code, selectedCourseIds) },
        dialogIcon = if (subject == null) Icons.Default.Add else Icons.Default.Edit,
        dialogTitle = title,
        confirmEnabled = name.isNotBlank() && code.isNotBlank()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Subject Name") },
            placeholder = { Text("e.g. Mobile Application Development") },
            leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Subject Code") },
            placeholder = { Text("e.g. MOBDEVA") },
            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactItemCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.DeleteConfirmationDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.InfoBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SearchBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SubjectFormDialog
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SuccessBanner
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyExitTransition
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageSubjectsScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {
    rememberCoroutineScope()

    val teacherViewModel = sharedViewModels.teacherViewModel
    val subjectViewModel = sharedViewModels.subjectViewModel
    val courseViewModel = sharedViewModels.courseViewModel

    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val subjects by subjectViewModel.teacherSubjects.collectAsState()
    val courses by courseViewModel.teacherCourses.collectAsState()
    val isLoading by subjectViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showManageCoursesSheet by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }

    var previousSubjectCount by remember { mutableIntStateOf(subjects.size) }
    LaunchedEffect(subjects.size) {
        if (subjects.size > previousSubjectCount) {
            successMessage = "Subject added successfully!"
            showSuccessBanner = true
            delay(3000)
            showSuccessBanner = false
        }
        previousSubjectCount = subjects.size
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter {
            it.subjectName.contains(searchQuery, ignoreCase = true) ||
                    it.subjectCode.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(currentTeacher) {
        currentTeacher?.let {
            subjectViewModel.loadSubjectsByTeacher(it.teacherId)
            courseViewModel.loadCoursesByTeacher(it.teacherId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Subjects",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${subjects.size} subject${if (subjects.size != 1) "s" else ""}",
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
                visible = subjects.isEmpty() && showContent,
                enter = bouncyEnterTransition(fromBottom = false),
                exit = bouncyExitTransition(toBottom = false)
            ) {
                InfoBanner(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    title = "Getting Started",
                    message = "Subjects are used for taking attendance. Create subjects for each class you teach."
                )
            }

            AnimatedVisibility(
                visible = showContent,
                enter = bouncyEnterTransition(fromBottom = false)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search subjects...",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredSubjects.isEmpty()) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition()
                ) {
                    if (searchQuery.isNotEmpty()) {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Search,
                            title = "No Subjects Found",
                            subtitle = "No subjects match \"$searchQuery\""
                        )
                    } else {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "No Subjects Yet",
                            subtitle = "Create subjects for the classes you teach",
                            actionLabel = "Create Subject",
                            onActionClick = { showAddDialog = true }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredSubjects, key = { _, subject -> subject.subjectId }) { index, subject ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = bouncyEnterTransition(index = index)
                        ) {
                            CompactItemCard(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                iconTint = Color(0xFFFF9800),
                                title = subject.subjectName,
                                subtitle = subject.subjectCode + if (subject.description.isNotEmpty()) " • ${subject.description}" else "",
                                onEdit = {
                                    selectedSubject = subject
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedSubject = subject
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
        SubjectFormDialog(
            title = "Add Subject",
            courses = courses,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, code, courseIds ->
                currentTeacher?.let {
                    subjectViewModel.addSubjectWithCourses(
                        subjectName = name,
                        subjectCode = code,
                        teacherId = it.teacherId,
                        courseIds = courseIds
                    )
                }
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && selectedSubject != null) {
        SubjectFormDialog(
            title = "Edit Subject",
            subject = selectedSubject,
            courses = courses,
            onDismiss = {
                showEditDialog = false
                selectedSubject = null
            },
            onConfirm = { name, code, courseIds ->
                selectedSubject?.let {
                    subjectViewModel.updateSubject(
                        it.copy(
                            subjectName = name,
                            subjectCode = code,
                        )
                    )
                    subjectViewModel.updateSubjectCourses(it.subjectId, courseIds)
                }
                showEditDialog = false
                selectedSubject = null
            }
        )
    }

    if (showDeleteDialog && selectedSubject != null) {
        DeleteConfirmationDialog(
            itemName = selectedSubject?.subjectName ?: "",
            itemType = "Subject",
            additionalMessage = "All attendance records for this subject will also be deleted.",
            onConfirm = {
                selectedSubject?.let { subjectViewModel.deleteSubject(it) }
                showDeleteDialog = false
                selectedSubject = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedSubject = null
            }
        )
    }

    if (showManageCoursesSheet && selectedSubject != null) {
        ManageSubjectCoursesSheet(
            subject = selectedSubject!!,
            allCourses = courses,
            subjectViewModel = subjectViewModel,
            onDismiss = {
                showManageCoursesSheet = false
                selectedSubject = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectCard(
    subject: Subject,
    courses: List<Course>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageCourses: () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subject.subjectCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (subject.description.isNotEmpty()) {
                        Text(
                            text = subject.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = onManageCourses,
                label = { Text("Manage Courses") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageSubjectCoursesSheet(
    subject: Subject,
    allCourses: List<Course>,
    subjectViewModel: com.markjayson545.mjdc_applicationcompose.bridge.viewmodels.SubjectViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    rememberCoroutineScope()

    val selectedCourseIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(subject) {
        subjectViewModel.loadSubjectsByCourse(subject.subjectId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Manage Courses for ${subject.subjectName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select courses to assign this subject to:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (allCourses.isEmpty()) {
                Text(
                    text = "No courses available. Create a course first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCourses) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCourseIds.contains(course.courseId),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedCourseIds.add(course.courseId)
                                            subjectViewModel.assignSubjectToCourse(
                                                subject.subjectId,
                                                course.courseId
                                            )
                                        } else {
                                            selectedCourseIds.remove(course.courseId)
                                            subjectViewModel.removeSubjectFromCourse(
                                                subject.subjectId,
                                                course.courseId
                                            )
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = course.courseName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = course.courseCode,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

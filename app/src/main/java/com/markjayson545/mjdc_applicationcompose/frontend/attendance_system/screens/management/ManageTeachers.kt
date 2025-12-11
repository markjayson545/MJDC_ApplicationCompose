/**
 * ============================================================================
 * MANAGE TEACHERS SCREEN (TEACHER DIRECTORY)
 * ============================================================================
 *
 * This screen displays a directory of all registered teachers in the system.
 * It's primarily a read-only view for discovering other teachers.
 *
 * SCREEN RESPONSIBILITIES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Display list of all registered teachers                             │
 * │ 2. Provide search functionality to filter teachers                     │
 * │ 3. Highlight current user in the list                                  │
 * │ 4. Show teacher contact information (name, email)                      │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * DATA FLOW:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SharedViewModels                                                       │
 * │       │                                                                 │
 * │       └── teacherViewModel                                              │
 * │            ├── currentTeacher → For highlighting current user          │
 * │            ├── teachers → All teachers list (reactive)                 │
 * │            └── loadAllTeachers() → Refresh teacher list                │
 * │                                                                         │
 * │  REPOSITORY METHODS USED:                                               │
 * │  - TeacherRepository.getAllTeachers() for complete list                │
 * │  - TeacherRepository.searchTeachers() could be integrated              │
 * │  - TeacherRepository.getTeacherWithAllData() for detailed view         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * UI SECTIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 1. Large Top App Bar - Title with teacher count                        │
 * │ 2. Search Bar - Filter teachers by name or email                       │
 * │ 3. Teacher List - Cards with name and email                            │
 * │    - Current user card highlighted with accent color                   │
 * │    - "(You)" label shown for current user                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * NOTE: This screen is read-only. Teachers cannot modify other teachers'
 * data. To edit own profile, use the Settings screen.
 *
 * @param navController Navigation controller for screen transitions
 * @param sharedViewModels Shared ViewModels containing all app state
 * @param onMenuClick Callback to open the navigation drawer
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.management

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.CompactItemCard
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.EmptyStateView
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components.SearchBar
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils.bouncyEnterTransition
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ManageTeachersScreen(
    navController: NavController,
    sharedViewModels: SharedViewModels,
    onMenuClick: () -> Unit = {}
) {
    val teacherViewModel = sharedViewModels.teacherViewModel
    val currentTeacher by teacherViewModel.currentTeacher.collectAsState()
    val teachers by teacherViewModel.teachers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }

    // Trigger animation
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val filteredTeachers = remember(teachers, searchQuery) {
        if (searchQuery.isBlank()) teachers
        else teachers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        teacherViewModel.loadAllTeachers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Teachers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${teachers.size} teacher${if (teachers.size != 1) "s" else ""} registered",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            AnimatedVisibility(
                visible = showContent,
                enter = bouncyEnterTransition(fromBottom = false)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search teachers...",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                )
            }

            if (filteredTeachers.isEmpty()) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = bouncyEnterTransition()
                ) {
                    if (searchQuery.isNotEmpty()) {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Search,
                            title = "No Teachers Found",
                            subtitle = "No teachers match \"$searchQuery\""
                        )
                    } else {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.School,
                            title = "No Teachers Yet",
                            subtitle = "No teachers have registered yet"
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredTeachers, key = { _, teacher -> teacher.teacherId }) { index, teacher ->
                        val isCurrentUser = teacher.teacherId == currentTeacher?.teacherId
                        AnimatedVisibility(
                            visible = showContent,
                            enter = bouncyEnterTransition(index = index)
                        ) {
                            CompactItemCard(
                                icon = Icons.Default.Person,
                                iconTint = if (isCurrentUser) MaterialTheme.colorScheme.primary else Color(0xFF607D8B),
                                title = teacher.fullName + if (isCurrentUser) " (You)" else "",
                                subtitle = teacher.email
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

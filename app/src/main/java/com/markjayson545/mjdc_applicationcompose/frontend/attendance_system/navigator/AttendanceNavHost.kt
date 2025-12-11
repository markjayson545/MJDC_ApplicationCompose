/**
 * ============================================================================
 * ATTENDANCE NAVIGATION HOST
 * ============================================================================
 *
 * This is the main navigation controller for the attendance system. It manages
 * all screen transitions, navigation state, and the navigation drawer/bottom bar.
 *
 * NAVIGATION ARCHITECTURE:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                     NAVIGATION FLOW                                     │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │   ┌─────────────┐     Login Success      ┌─────────────────────────┐   │
 * │   │ Auth Screen │ ────────────────────►  │   Main App Container    │   │
 * │   └─────────────┘                        │  ┌───────────────────┐  │   │
 * │         ▲                                │  │ Navigation Drawer │  │   │
 * │         │                                │  └───────────────────┘  │   │
 * │      Logout                              │  ┌───────────────────┐  │   │
 * │         │                                │  │   Screen Content  │  │   │
 * │         └────────────────────────────────│  └───────────────────┘  │   │
 * │                                          │  ┌───────────────────┐  │   │
 * │                                          │  │ Bottom Navigation │  │   │
 * │                                          │  └───────────────────┘  │   │
 * │                                          └─────────────────────────┘   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * AUTHENTICATION STATE:
 * - isLoggedIn = false → Shows AuthScreen (login/register)
 * - isLoggedIn = true  → Shows main app with drawer and bottom nav
 *
 * SCREEN CATEGORIES:
 * 1. Bottom Nav Screens (AttendanceDestinations):
 *    - Dashboard, Attendance
 *    - Always show bottom navigation bar
 *
 * 2. Drawer Screens (DrawerDestinations):
 *    - Students, Courses, Subjects, Teachers, Reports, Settings
 *    - No bottom navigation bar (cleaner UI)
 *
 * LOGOUT FLOW:
 * 1. User taps Logout in drawer
 * 2. Drawer closes
 * 3. sharedViewModels.clearAllData() clears session
 * 4. Navigate to AuthRoutes.AUTH with cleared backstack
 * 5. User sees login screen
 *
 * @param sharedViewModels Shared ViewModels containing all app state
 * @param startDestination Initial route (defaults to auth screen)
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.AttendanceDashboardScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.AuthScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.ManageAttendanceScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.ManageCoursesScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.ManageStudentsScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.ManageSubjectsScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.ManageTeachersScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.ReportsScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.screens.SettingsScreen
import kotlinx.coroutines.launch

/**
 * Main navigation host composable that manages all screen transitions.
 *
 * This composable:
 * 1. Creates and manages the NavController
 * 2. Observes authentication state to show auth or main screens
 * 3. Provides navigation drawer with user profile and menu items
 * 4. Manages bottom navigation for primary screens
 *
 * @param sharedViewModels Shared ViewModels for all app data
 * @param startDestination Starting route (default: AuthRoutes.AUTH)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceNavHost(
    sharedViewModels: SharedViewModels,
    startDestination: String = AuthRoutes.AUTH
) {
    // Navigation controller manages backstack and screen transitions
    val navController = rememberNavController()

    // Drawer state controls open/close of navigation drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Coroutine scope for launching drawer animations
    val scope = rememberCoroutineScope()

    // Observe authentication state from TeacherViewModel
    val currentTeacher by sharedViewModels.teacherViewModel.currentTeacher.collectAsState()
    val isLoggedIn by sharedViewModels.teacherViewModel.isLoggedIn.collectAsState()

    // Track current route for highlighting active navigation items
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if current route should show drawer/bottom nav
    // Main screens show full navigation UI, auth screens don't
    val isMainScreen = AttendanceDestinations.entries.any { it.route == currentRoute } ||
            DrawerDestinations.entries.any { it.route == currentRoute }

    // ========================================================================
    // MAIN APP CONTAINER (shown when logged in)
    // ========================================================================
    if (isLoggedIn && isMainScreen) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            // Only allow swipe gestures when drawer is open (prevents accidental opens)
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                // Navigation drawer content with profile header and menu items
                ModalDrawerSheet {
                    // --------------------------------------------------------
                    // USER PROFILE HEADER
                    // Shows avatar, name, and email of current teacher
                    // --------------------------------------------------------
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar circle with person icon
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Teacher's full name
                        Text(
                            text = currentTeacher?.fullName ?: "Teacher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Teacher's email
                        Text(
                            text = currentTeacher?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // --------------------------------------------------------
                    // MAIN NAVIGATION ITEMS (Bottom Nav destinations)
                    // Dashboard and Attendance - most frequently used
                    // --------------------------------------------------------
                    Text(
                        text = "Main",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    AttendanceDestinations.entries.forEach { destination ->
                        NavigationDrawerItem(
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                            selected = currentRoute == destination.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigateTo(destination.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // --------------------------------------------------------
                    // MANAGEMENT SECTION
                    // Students, Courses, Subjects management
                    // --------------------------------------------------------
                    Text(
                        text = "Management",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Show management destinations (Students, Courses, Subjects)
                    listOf(
                        DrawerDestinations.MANAGE_STUDENTS,
                        DrawerDestinations.MANAGE_COURSES,
                        DrawerDestinations.MANAGE_SUBJECTS
                    ).forEach { destination ->
                        NavigationDrawerItem(
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                            selected = currentRoute == destination.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigateTo(destination.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // --------------------------------------------------------
                    // MORE SECTION
                    // Teachers, Reports, Settings
                    // --------------------------------------------------------
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Show remaining drawer destinations
                    listOf(
                        DrawerDestinations.MANAGE_TEACHERS,
                        DrawerDestinations.REPORTS,
                        DrawerDestinations.SETTINGS
                    ).forEach { destination ->
                        NavigationDrawerItem(
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                            selected = currentRoute == destination.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigateTo(destination.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider()

                    // --------------------------------------------------------
                    // LOGOUT BUTTON
                    // Clears session and navigates to auth screen
                    // --------------------------------------------------------
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null
                            )
                        },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                // 1. Close the drawer first
                                drawerState.close()
                            }
                            // 2. Clear all user data and session
                            // This will set isLoggedIn to false, triggering recomposition
                            // to show the auth NavHost instead
                            sharedViewModels.clearAllData()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        ) {
            // Main content area with NavHost and bottom navigation
            MainContent(
                navController = navController,
                sharedViewModels = sharedViewModels,
                currentRoute = currentRoute,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        }
    } else {
        // ====================================================================
        // AUTH SCREENS (shown when not logged in)
        // ====================================================================
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Login/Register screen
            composable(AuthRoutes.AUTH) {
                AuthScreen(
                    navController = navController,
                    sharedViewModels = sharedViewModels
                )
            }

            // Dashboard (needed for navigation after login)
            composable(AttendanceDestinations.DASHBOARD.route) {
                AttendanceDashboardScreen(
                    navController = navController,
                    sharedViewModels = sharedViewModels
                )
            }
        }
    }
}

/**
 * Main content area containing the NavHost and bottom navigation bar.
 *
 * This composable handles:
 * 1. Screen content rendering via NavHost
 * 2. Bottom navigation bar (only for primary screens)
 *
 * BOTTOM NAV VISIBILITY:
 * - Shown: Dashboard, Attendance (AttendanceDestinations)
 * - Hidden: All drawer destinations (cleaner UI for management screens)
 *
 * @param navController NavHostController for navigation
 * @param sharedViewModels Shared ViewModels for all screens
 * @param currentRoute Current route string for highlighting active items
 * @param onMenuClick Callback to open navigation drawer
 */
@Composable
private fun MainContent(
    navController: NavHostController,
    sharedViewModels: SharedViewModels,
    currentRoute: String?,
    onMenuClick: () -> Unit
) {
    // Only show bottom nav for primary destinations (Dashboard, Attendance)
    val isBottomNavDestination = AttendanceDestinations.entries.any { it.route == currentRoute }

    Column(modifier = Modifier.fillMaxSize()) {
        // Screen content area (takes remaining space after bottom nav)
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = AttendanceDestinations.DASHBOARD.route
            ) {
                // ============================================================
                // PRIMARY SCREENS (Bottom Navigation)
                // ============================================================

                // Dashboard - main overview screen
                composable(AttendanceDestinations.DASHBOARD.route) {
                    AttendanceDashboardScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // Attendance management - record and view check-ins
                composable(AttendanceDestinations.MANAGE_ATTENDANCE.route) {
                    ManageAttendanceScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // ============================================================
                // MANAGEMENT SCREENS (Drawer Navigation)
                // ============================================================

                // Student management - add, edit, delete students
                composable(DrawerDestinations.MANAGE_STUDENTS.route) {
                    ManageStudentsScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // Course management - create and organize courses
                composable(DrawerDestinations.MANAGE_COURSES.route) {
                    ManageCoursesScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // Subject management - create subjects and link to courses
                composable(DrawerDestinations.MANAGE_SUBJECTS.route) {
                    ManageSubjectsScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // ============================================================
                // INFORMATION SCREENS (Drawer Navigation)
                // ============================================================

                // Teachers directory - view all registered teachers
                composable(DrawerDestinations.MANAGE_TEACHERS.route) {
                    ManageTeachersScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // Reports and analytics dashboard
                composable(DrawerDestinations.REPORTS.route) {
                    ReportsScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }

                // ============================================================
                // SYSTEM SCREENS (Drawer Navigation)
                // ============================================================

                // Settings - profile management, app preferences
                composable(DrawerDestinations.SETTINGS.route) {
                    SettingsScreen(
                        navController = navController,
                        sharedViewModels = sharedViewModels,
                        onMenuClick = onMenuClick
                    )
                }
            }
        }

        // ====================================================================
        // BOTTOM NAVIGATION BAR
        // Only shown for primary screens (Dashboard, Attendance)
        // ====================================================================
        if (isBottomNavDestination) {
            NavigationBar {
                AttendanceDestinations.entries.forEach { destination ->
                    NavigationBarItem(
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.label) },
                        selected = currentRoute == destination.route,
                        onClick = { navController.navigateTo(destination.route) }
                    )
                }
            }
        }
    }
}

/**
 * Extension function for simplified navigation with backstack management.
 *
 * This function:
 * 1. Navigates to the specified route
 * 2. Pops backstack to start destination (prevents deep backstack)
 * 3. Saves state for restoration when returning
 * 4. Uses launchSingleTop to avoid duplicate destinations
 *
 * @param route Destination route string
 */
private fun NavController.navigateTo(route: String) {
    navigate(route) {
        // Pop up to the start destination, saving state
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }
}

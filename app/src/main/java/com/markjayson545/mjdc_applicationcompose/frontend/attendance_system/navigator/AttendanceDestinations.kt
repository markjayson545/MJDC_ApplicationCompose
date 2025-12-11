/**
 * ============================================================================
 * ATTENDANCE NAVIGATION DESTINATIONS
 * ============================================================================
 *
 * This file defines all navigation routes and destinations for the attendance
 * system. Routes are organized into three categories:
 *
 * NAVIGATION STRUCTURE:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      NAVIGATION HIERARCHY                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │ Bottom Navigation (AttendanceDestinations)                       │   │
 * │  │ - Primary screens always visible in bottom bar                   │   │
 * │  │ - Dashboard: Main overview screen                                │   │
 * │  │ - Attendance: Take/manage attendance                             │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                         │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │ Navigation Drawer (DrawerDestinations)                           │   │
 * │  │ - Secondary screens accessed via hamburger menu                  │   │
 * │  │ - Students, Courses, Subjects management                         │   │
 * │  │ - Teachers directory, Reports, Settings                          │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                         │
 * │  ┌─────────────────────────────────────────────────────────────────┐   │
 * │  │ Auth Routes (AuthRoutes)                                         │   │
 * │  │ - Login/Register screens before authentication                   │   │
 * │  └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ROUTE NAMING CONVENTION:
 * - snake_case for route strings (e.g., "manage_attendance")
 * - SCREAMING_SNAKE_CASE for enum values (e.g., MANAGE_ATTENDANCE)
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Primary navigation destinations shown in the bottom navigation bar.
 *
 * These are the most frequently accessed screens and are always visible
 * at the bottom of the screen for quick access.
 *
 * SIMPLIFIED NAVIGATION:
 * - Only Dashboard and Attendance are in bottom nav
 * - Other screens moved to drawer for cleaner UI
 *
 * @property label Display text for the navigation item
 * @property icon Icon to show in the navigation bar
 * @property route Navigation route string used by NavHost
 */
enum class AttendanceDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    /** Main dashboard with overview statistics and recent activity */
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "attendance_dashboard"),

    /** Attendance management screen for recording check-ins */
    MANAGE_ATTENDANCE("Attendance", Icons.Default.CalendarMonth, "manage_attendance"),
}

/**
 * Secondary navigation destinations shown in the navigation drawer.
 *
 * These screens are accessed via the hamburger menu and include
 * management screens, reports, and settings.
 *
 * DRAWER ORGANIZATION:
 * - Management: Students, Courses, Subjects
 * - Information: Teachers directory, Reports
 * - System: Settings
 *
 * @property label Display text for the drawer item
 * @property icon Icon to show in the drawer
 * @property route Navigation route string used by NavHost
 */
enum class DrawerDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    /** Student management - add, edit, delete students */
    MANAGE_STUDENTS("Students", Icons.Default.Groups, "manage_students"),

    /** Course management - create and organize courses */
    MANAGE_COURSES("Courses", Icons.Default.Book, "manage_courses"),

    /** Subject management - create subjects and link to courses */
    MANAGE_SUBJECTS("Subjects", Icons.AutoMirrored.Filled.MenuBook, "manage_subjects"),

    /** View all registered teachers in the system */
    MANAGE_TEACHERS("Teachers", Icons.Default.School, "manage_teachers"),

    /** Reports and analytics dashboard */
    REPORTS("Reports", Icons.Default.Assessment, "reports"),

    /** Application settings and profile management */
    SETTINGS("Settings", Icons.Default.Settings, "settings"),
}

/**
 * Authentication-related routes.
 *
 * These routes are used before the user is authenticated and
 * handle the login/registration flow.
 */
object AuthRoutes {
    /** Main auth screen with login/register toggle */
    const val AUTH = "auth"

    /** Direct login route (if needed for deep linking) */
    const val LOGIN = "login"

    /** Direct register route (if needed for deep linking) */
    const val REGISTER = "register"
}
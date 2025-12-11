/**
 * ============================================================================
 * FILTER & SORT COMPONENTS
 * ============================================================================
 *
 * This file contains reusable filter and sort UI components using Material 3
 * Expressive design patterns including SplitButton and ButtonGroup.
 *
 * COMPONENT CATEGORIES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ FILTER COMPONENTS:                                                      │
 * │ - SubjectFilterSplitButton: Filter by subject with dropdown             │
 * │ - FilterButton: Opens filter dialog/bottom sheet                        │
 * │ - AttendanceStatusFilter: Filter by attendance status                   │
 * │                                                                         │
 * │ SORT COMPONENTS:                                                        │
 * │ - SortButton: Opens sort options menu                                   │
 * │ - SortButtonGroup: Toggle buttons for sort direction                    │
 * │                                                                         │
 * │ COMBINED COMPONENTS:                                                    │
 * │ - FilterSortRow: Row containing filter and sort buttons                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * DESIGN TOKENS:
 * - Uses Material 3 SplitButton and ButtonGroup (experimental API)
 * - Follows Material 3 Expressive motion scheme
 * - Filters and sorting are NOT persistent (in-memory only)
 *
 * USAGE IN SCREENS:
 * - ManageAttendance: Subject filter, status filter, date range filter
 * - ManageStudents: Course filter, name/ID sorting
 * - ManageCourses: Name/student count sorting
 * - ManageSubjects: Course assignment filter
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.AttendanceStatus
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Course
import com.markjayson545.mjdc_applicationcompose.backend.attendance_system.model.Subject

/**
 * Filter/Sort state models - NOT persistent, in-memory only
 */
data class AttendanceFilterState(
    val selectedSubjectId: String? = null,
    val selectedStatuses: Set<AttendanceStatus> = emptySet(),
    val dateRange: DateRangePreset = DateRangePreset.TODAY
)

data class StudentFilterState(
    val selectedCourseId: String? = null,
    val sortOption: StudentSortOption = StudentSortOption.NAME_ASC
)

data class CourseFilterState(
    val sortOption: CourseSortOption = CourseSortOption.NAME_ASC
)

data class SubjectFilterState(
    val selectedCourseId: String? = null,
    val sortOption: SubjectSortOption = SubjectSortOption.NAME_ASC
)

enum class DateRangePreset(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL("All Time")
}

enum class StudentSortOption(val label: String, val ascending: Boolean) {
    NAME_ASC("Name (A-Z)", true),
    NAME_DESC("Name (Z-A)", false),
    ID_ASC("ID (Ascending)", true),
    ID_DESC("ID (Descending)", false)
}

enum class CourseSortOption(val label: String, val ascending: Boolean) {
    NAME_ASC("Name (A-Z)", true),
    NAME_DESC("Name (Z-A)", false),
    STUDENT_COUNT_ASC("Student Count ↑", true),
    STUDENT_COUNT_DESC("Student Count ↓", false)
}

enum class SubjectSortOption(val label: String, val ascending: Boolean) {
    NAME_ASC("Name (A-Z)", true),
    NAME_DESC("Name (Z-A)", false),
    CODE_ASC("Code (A-Z)", true),
    CODE_DESC("Code (Z-A)", false)
}

enum class AttendanceSortOption(val label: String, val ascending: Boolean) {
    NAME_ASC("Name (A-Z)", true),
    NAME_DESC("Name (Z-A)", false),
    TIME_ASC("Time (Earliest)", true),
    TIME_DESC("Time (Latest)", false),
    STATUS("Status", true)
}

/**
 * Split Button for Subject Filter
 *
 * A split button where the leading segment shows the selected subject
 * and the trailing segment opens a dropdown to select a different subject.
 *
 * Based on Material 3 SplitButton design pattern.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubjectFilterSplitButton(
    modifier: Modifier = Modifier,
    subjects: List<Subject>,
    selectedSubject: Subject?,
    onSubjectSelected: (Subject?) -> Unit,
    label: String = "Subject"
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "splitButtonScale"
    )

    val hasSelection = selectedSubject != null
    val backgroundColor = if (hasSelection)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.scale(scale),
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            border = BorderStroke(
                width = 1.dp,
                color = if (hasSelection)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading segment - selected subject or label
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { if (hasSelection) onSubjectSelected(null) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSelection) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = selectedSubject?.subjectCode ?: label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (hasSelection) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (hasSelection)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                // Trailing segment - dropdown trigger
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select subject",
                        tint = if (hasSelection)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Clear selection option
            if (hasSelection) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Clear Filter",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        onSubjectSelected(null)
                        expanded = false
                    }
                )
            }

            subjects.forEach { subject ->
                val isSelected = selectedSubject?.subjectId == subject.subjectId
                DropdownMenuItem(
                    text = {
                        Text(
                            "${subject.subjectCode} - ${subject.subjectName}",
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    onClick = {
                        onSubjectSelected(subject)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Filter Button that opens a filter bottom sheet or dialog
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilterButton(
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false,
    activeFilterCount: Int = 0,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "filterButtonScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (hasActiveFilters)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "filterBgColor"
    )

    Surface(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (hasActiveFilters)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter",
                modifier = Modifier.size(18.dp),
                tint = if (hasActiveFilters)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Filter",
                style = MaterialTheme.typography.labelLarge,
                color = if (hasActiveFilters)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasActiveFilters && activeFilterCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = activeFilterCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sort Button that opens sort options menu
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SortButton(
    modifier: Modifier = Modifier,
    currentSort: T,
    sortOptions: List<T>,
    sortToLabel: (T) -> String,
    onSortSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "sortButtonScale"
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.scale(scale),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sort",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sortOptions.forEach { option ->
                val isSelected = option == currentSort
                DropdownMenuItem(
                    text = {
                        Text(
                            sortToLabel(option),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    onClick = {
                        onSortSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Attendance Status Filter using ButtonGroup pattern
 *
 * A horizontal row of toggle buttons for selecting attendance status filters.
 * Multiple statuses can be selected at once.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AttendanceStatusFilterGroup(
    modifier: Modifier = Modifier,
    selectedStatuses: Set<AttendanceStatus>,
    onStatusToggle: (AttendanceStatus) -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AttendanceStatus.entries.forEach { status ->
            val isSelected = status in selectedStatuses
            val statusColor = getStatusColor(status)

            FilterChip(
                selected = isSelected,
                onClick = { onStatusToggle(status) },
                label = {
                    Text(
                        text = getStatusLabel(status),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = statusColor.copy(alpha = 0.2f),
                    selectedLabelColor = statusColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = statusColor.copy(alpha = 0.5f),
                    selectedBorderColor = statusColor,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

/**
 * Filter Sort Row - Combined row containing subject filter, filter button, and sort button
 *
 * This is the main component to be placed after the SearchBar on management screens.
 */
@Composable
fun FilterSortRow(
    modifier: Modifier = Modifier,
    subjects: List<Subject> = emptyList(),
    selectedSubject: Subject? = null,
    onSubjectSelected: ((Subject?) -> Unit)? = null,
    hasActiveFilters: Boolean = false,
    activeFilterCount: Int = 0,
    onFilterClick: () -> Unit = {},
    sortLabel: String = "Sort",
    onSortClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subject filter split button (if subjects provided)
        if (subjects.isNotEmpty() && onSubjectSelected != null) {
            SubjectFilterSplitButton(
                subjects = subjects,
                selectedSubject = selectedSubject,
                onSubjectSelected = onSubjectSelected
            )
        }

        // Filter button
        FilterButton(
            hasActiveFilters = hasActiveFilters,
            activeFilterCount = activeFilterCount,
            onClick = onFilterClick
        )

        // Sort button placeholder - actual implementation depends on context
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            onClick = onSortClick
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Sort",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sortLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Date Range Filter using preset options
 */
@Composable
fun DateRangeFilterChips(
    modifier: Modifier = Modifier,
    selectedPreset: DateRangePreset,
    onPresetSelected: (DateRangePreset) -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DateRangePreset.entries.forEach { preset ->
            val isSelected = preset == selectedPreset

            FilterChip(
                selected = isSelected,
                onClick = { onPresetSelected(preset) },
                label = {
                    Text(
                        text = preset.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Attendance Filter Bottom Sheet
 *
 * Full filter configuration for attendance management screen.
 * Includes status filter, date range, and subject selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceFilterBottomSheet(
    isVisible: Boolean,
    subjects: List<Subject>,
    currentFilter: AttendanceFilterState,
    onFilterChanged: (AttendanceFilterState) -> Unit,
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Attendance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentFilter.selectedSubjectId != null ||
                        currentFilter.selectedStatuses.isNotEmpty() ||
                        currentFilter.dateRange != DateRangePreset.TODAY) {
                        FilledTonalButton(
                            onClick = onClearFilters,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date Range Section
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                DateRangeFilterChips(
                    selectedPreset = currentFilter.dateRange,
                    onPresetSelected = { onFilterChanged(currentFilter.copy(dateRange = it)) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Status Section
                Text(
                    text = "Attendance Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                AttendanceStatusFilterGroup(
                    selectedStatuses = currentFilter.selectedStatuses,
                    onStatusToggle = { status ->
                        val newStatuses = if (status in currentFilter.selectedStatuses) {
                            currentFilter.selectedStatuses - status
                        } else {
                            currentFilter.selectedStatuses + status
                        }
                        onFilterChanged(currentFilter.copy(selectedStatuses = newStatuses))
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Subject Section
                if (subjects.isNotEmpty()) {
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SubjectFilterSplitButton(
                        subjects = subjects,
                        selectedSubject = subjects.find { it.subjectId == currentFilter.selectedSubjectId },
                        onSubjectSelected = { subject ->
                            onFilterChanged(currentFilter.copy(selectedSubjectId = subject?.subjectId))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Apply button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

/**
 * Attendance Sort Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSortBottomSheet(
    isVisible: Boolean,
    currentSort: AttendanceSortOption,
    onSortSelected: (AttendanceSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                AttendanceSortOption.entries.forEach { option ->
                    val isSelected = option == currentSort
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        onClick = {
                            onSortSelected(option)
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (option.ascending)
                                    Icons.Default.ArrowUpward
                                else
                                    Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to get status color
 */
@Composable
fun getStatusColor(status: AttendanceStatus): Color {
    return when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF4CAF50)  // Green
        AttendanceStatus.ABSENT -> Color(0xFFF44336)   // Red
        AttendanceStatus.LATE -> Color(0xFFFF9800)     // Orange
        AttendanceStatus.EXCUSED -> Color(0xFF2196F3) // Blue
    }
}

/**
 * Helper function to get status label
 */
fun getStatusLabel(status: AttendanceStatus): String {
    return when (status) {
        AttendanceStatus.PRESENT -> "Present"
        AttendanceStatus.ABSENT -> "Absent"
        AttendanceStatus.LATE -> "Late"
        AttendanceStatus.EXCUSED -> "Excused"
    }
}

/**
 * Edit Attendance Bottom Sheet
 *
 * Allows modifying an existing check-in's status or removing it entirely.
 *
 * @param isVisible Whether the sheet is visible
 * @param studentName Name of the student
 * @param currentStatus Current attendance status
 * @param subjectCode Subject code for display
 * @param checkInTime Time of check-in
 * @param onStatusChange Callback when status is changed
 * @param onRemoveCheckIn Callback when check-in is removed (student becomes absent)
 * @param onDismiss Callback when sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAttendanceBottomSheet(
    isVisible: Boolean,
    studentName: String,
    currentStatus: AttendanceStatus,
    subjectCode: String,
    checkInTime: String?,
    onStatusChange: (AttendanceStatus) -> Unit,
    onRemoveCheckIn: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()
        var selectedStatus by remember(currentStatus) { mutableStateOf(currentStatus) }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Text(
                    text = "Edit Attendance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Student info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = studentName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = studentName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$subjectCode${checkInTime?.let { " • $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Current status badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = getStatusColor(currentStatus).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = getStatusLabel(currentStatus),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = getStatusColor(currentStatus),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Status selection
                Text(
                    text = "Change Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status options as cards
                AttendanceStatus.entries.forEach { status ->
                    val isSelected = status == selectedStatus
                    val statusColor = getStatusColor(status)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                statusColor.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) BorderStroke(2.dp, statusColor) else null,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { selectedStatus = status }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = getStatusLabel(status),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = statusColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Remove check-in button
                    OutlinedButton(
                        onClick = onRemoveCheckIn,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove")
                    }

                    // Save button
                    Button(
                        onClick = {
                            if (selectedStatus != currentStatus) {
                                onStatusChange(selectedStatus)
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedStatus != currentStatus
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update")
                    }
                }
            }
        }
    }
}

/**
 * Student Filter Bottom Sheet
 *
 * Allows filtering students by course.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFilterBottomSheet(
    isVisible: Boolean,
    courses: List<Course>,
    selectedCourseId: String?,
    onCourseSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Students",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedCourseId != null) {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Course filter section
                Text(
                    text = "By Course",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // All courses option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedCourseId == null)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (selectedCourseId == null)
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else null,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        onCourseSelected(null)
                        onDismiss()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Courses",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedCourseId == null) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedCourseId == null) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Course options
                courses.forEach { course ->
                    val isSelected = course.courseId == selectedCourseId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            onCourseSelected(course.courseId)
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.courseName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = course.courseCode,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Student Sort Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSortBottomSheet(
    isVisible: Boolean,
    currentSort: StudentSortOption,
    onSortSelected: (StudentSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                StudentSortOption.entries.forEach { option ->
                    val isSelected = option == currentSort

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            onSortSelected(option)
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (option.ascending)
                                    Icons.Default.ArrowUpward
                                else
                                    Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Course Sort Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSortBottomSheet(
    isVisible: Boolean,
    currentSort: CourseSortOption,
    onSortSelected: (CourseSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                CourseSortOption.entries.forEach { option ->
                    val isSelected = option == currentSort

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            onSortSelected(option)
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (option.ascending)
                                    Icons.Default.ArrowUpward
                                else
                                    Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Subject Sort Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSortBottomSheet(
    isVisible: Boolean,
    currentSort: SubjectSortOption,
    onSortSelected: (SubjectSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                SubjectSortOption.entries.forEach { option ->
                    val isSelected = option == currentSort

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else null,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            onSortSelected(option)
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (option.ascending)
                                    Icons.Default.ArrowUpward
                                else
                                    Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


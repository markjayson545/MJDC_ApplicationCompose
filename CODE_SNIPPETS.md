# Key Code Snippets Reference

## FAB Implementation

### Add FAB to Scaffold
```kotlin
Scaffold(
    topBar = { /* ... */ },
    floatingActionButton = {
        FloatingActionButton(
            onClick = {
                resetForm()
                showBottomSheet = true
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }
    }
) { innerPadding ->
    // Screen content
}
```

---

## BottomSheet Implementation

### Using InputBottomSheet Component
```kotlin
InputBottomSheet(
    isVisible = showBottomSheet,
    title = if (isEditMode) "Edit Course" else "Add New Course",
    isEditMode = isEditMode,
    onDismiss = {
        showBottomSheet = false
        resetForm()
    },
    onSaveClick = { saveCourse() }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ManagementTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = "Course Name",
            placeholder = "e.g., Mobile App Development",
            icon = Icons.Default.TextFields
        )
        Spacer(modifier = Modifier.height(12.dp))
        ManagementTextField(
            value = courseCode,
            onValueChange = { courseCode = it },
            label = "Course Code",
            placeholder = "e.g., MD101",
            icon = Icons.Default.TextFields
        )
    }
}
```

---

## LazyColumn with ItemsIndexed

```kotlin
LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
    itemsIndexed(items) { index, item ->
        ManagementItemCard(
            itemNumber = (index + 1).toString(),
            title = item.name,
            subtitle = item.code,
            description = item.description,
            onEdit = {
                // Populate form
                courseName = item.courseName
                courseCode = item.courseCode
                editingCourseId = index.toLong()
                isEditMode = true
                showBottomSheet = true
            },
            onDelete = {
                courseToDelete = item.courseId
                showDeleteDialog = true
            }
        )
    }
}
```

---

## Delete Confirmation Dialog

```kotlin
DeleteConfirmationDialog(
    isVisible = showDeleteDialog,
    title = "Delete Course",
    message = "Are you sure you want to delete this course?",
    onConfirm = {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                courseDao.deleteCourse(courseToDelete)
            }
            if (result > 0) {
                Toast.makeText(context, "Course deleted successfully.", Toast.LENGTH_SHORT).show()
                val updatedCourses = withContext(Dispatchers.IO) {
                    courseDao.getAllCourses()
                }
                courses.clear()
                courses.addAll(updatedCourses)
            }
            showDeleteDialog = false
        }
    },
    onDismiss = {
        showDeleteDialog = false
        courseToDelete = ""
    }
)
```

---

## Initial Data Loading

```kotlin
LaunchedEffect(Unit) {
    scope.launch {
        val coursesList = withContext(Dispatchers.IO) {
            courseDao.getAllCourses()
        }
        courses.clear()
        courses.addAll(coursesList)
    }
}
```

---

## Save Function - Create vs Update

```kotlin
fun saveCourse() {
    if (courseName.isNotEmpty() && courseCode.isNotEmpty()) {
        scope.launch {
            if (isEditMode) {
                // UPDATE MODE
                val editingCourse = courses.firstOrNull { 
                    it.courseId.endsWith(editingCourseId.toString()) 
                }
                if (editingCourse != null) {
                    val result = withContext(Dispatchers.IO) {
                        courseDao.updateCourse(
                            editingCourse.courseId, 
                            courseName, 
                            courseCode
                        )
                    }
                    if (result > 0) {
                        Toast.makeText(context, "Course updated successfully.", Toast.LENGTH_SHORT).show()
                        val updatedCourses = withContext(Dispatchers.IO) {
                            courseDao.getAllCourses()
                        }
                        courses.clear()
                        courses.addAll(updatedCourses)
                    }
                }
            } else {
                // CREATE MODE
                val status = withContext(Dispatchers.IO) {
                    courseDao.insertCourse(
                        courseName = courseName, 
                        courseCode = courseCode
                    )
                }
                if (status < 0) {
                    Toast.makeText(context, "Failed to save course.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Course saved successfully.", Toast.LENGTH_SHORT).show()
                    val updatedCourses = withContext(Dispatchers.IO) {
                        courseDao.getAllCourses()
                    }
                    courses.clear()
                    courses.addAll(updatedCourses)
                }
            }
            resetForm()
            showBottomSheet = false
        }
    } else {
        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
    }
}
```

---

## Form Reset Function

```kotlin
fun resetForm() {
    courseName = ""
    courseCode = ""
    isEditMode = false
    editingCourseId = 0L
}
```

---

## Empty State Display

```kotlin
if (courses.isEmpty()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No courses yet. Click the + button to add one!")
    }
} else {
    // Show LazyColumn with items
}
```

---

## ManagementItemCard Component

```kotlin
@Composable
fun ManagementItemCard(
    modifier: Modifier = Modifier,
    itemNumber: String,
    title: String,
    subtitle: String = "",
    description: String = "",
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    itemNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Information
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }

            // Action buttons
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
```

---

## InputBottomSheet Component

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBottomSheet(
    isVisible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    isEditMode: Boolean = false,
    onSaveClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                content()
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isEditMode) "Update" else "Save")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
```

---

## ManagementTextField Component

```kotlin
@Composable
fun ManagementTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    icon: ImageVector? = null,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        modifier = modifier.padding(horizontal = 0.dp),
        value = value,
        onValueChange = onValueChange,
        leadingIcon = if (icon != null) {
            { Icon(icon, contentDescription = label) }
        } else null,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        }
    )
}
```

---

## DeleteConfirmationDialog Component

```kotlin
@Composable
fun DeleteConfirmationDialog(
    isVisible: Boolean,
    title: String = "Delete Item",
    message: String = "Are you sure you want to delete this item?",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Red
                )
            },
            title = { Text(title) },
            text = { Text(message) },
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

---

## State Variables Setup

```kotlin
// Input fields
var courseName by remember { mutableStateOf("") }
var courseCode by remember { mutableStateOf("") }

// UI visibility states
var showBottomSheet by remember { mutableStateOf(false) }
var showDeleteDialog by remember { mutableStateOf(false) }

// Edit mode tracking
var isEditMode by remember { mutableStateOf(false) }
var editingCourseId by remember { mutableLongStateOf(0L) }

// List management
val courses = remember { mutableStateListOf<Course>() }

// Delete tracking
var courseToDelete by remember { mutableStateOf("") }
```

---

## DAO Methods - CourseDao

```kotlin
fun getAllCourses(): List<Course> {
    val courses = mutableListOf<Course>()
    val cursor = databaseHelper.readableDatabase.query(
        AttendanceDbHelper.TABLE_COURSE,
        null, null, null, null, null, null
    )
    with(cursor) {
        while (moveToNext()) {
            val course = Course(
                courseId = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_ID)),
                courseName = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_NAME)),
                courseCode = getString(getColumnIndexOrThrow(AttendanceDbHelper.COLUMN_COURSE_CODE)),
                courseDescription = ""
            )
            courses.add(course)
        }
    }
    cursor.close()
    return courses
}

fun updateCourse(courseId: String, courseName: String, courseCode: String): Int {
    val values = ContentValues().apply {
        put(AttendanceDbHelper.COLUMN_COURSE_NAME, courseName)
        put(AttendanceDbHelper.COLUMN_COURSE_CODE, courseCode)
    }
    return databaseHelper.writableDatabase.update(
        AttendanceDbHelper.TABLE_COURSE,
        values,
        "${AttendanceDbHelper.COLUMN_COURSE_ID} = ?",
        arrayOf(courseId)
    )
}

fun deleteCourse(courseId: String): Int {
    return databaseHelper.writableDatabase.delete(
        AttendanceDbHelper.TABLE_COURSE,
        "${AttendanceDbHelper.COLUMN_COURSE_ID} = ?",
        arrayOf(courseId)
    )
}
```

---

## Complete Screen State Management Pattern

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManagement(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form state
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    
    // UI state
    var showBottomSheet by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingCourseId by remember { mutableLongStateOf(0L) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var courseToDelete by remember { mutableStateOf("") }
    
    // Data
    val courses = remember { mutableStateListOf<Course>() }
    val dbHelper = AttendanceDbHelper(context)
    val courseDao = CourseDao(dbHelper)

    // Load initial data
    LaunchedEffect(Unit) {
        scope.launch {
            val coursesList = withContext(Dispatchers.IO) {
                courseDao.getAllCourses()
            }
            courses.clear()
            courses.addAll(coursesList)
        }
    }

    // Helper functions
    fun resetForm() {
        courseName = ""
        courseCode = ""
        isEditMode = false
        editingCourseId = 0L
    }

    fun saveCourse() {
        if (courseName.isNotEmpty() && courseCode.isNotEmpty()) {
            scope.launch {
                if (isEditMode) {
                    // Update logic
                } else {
                    // Insert logic
                }
                resetForm()
                showBottomSheet = false
            }
        }
    }

    // UI
    Scaffold(
        topBar = { /* ... */ },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                resetForm()
                showBottomSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        // Screen content
    }

    // Components
    InputBottomSheet(
        isVisible = showBottomSheet,
        title = if (isEditMode) "Edit" else "Add",
        isEditMode = isEditMode,
        onDismiss = {
            showBottomSheet = false
            resetForm()
        },
        onSaveClick = { saveCourse() }
    ) {
        // Form fields
    }

    DeleteConfirmationDialog(
        isVisible = showDeleteDialog,
        onConfirm = { /* delete logic */ },
        onDismiss = {
            showDeleteDialog = false
            courseToDelete = ""
        }
    )
}
```

---

## Coroutine Pattern for Database Operations

```kotlin
// Safe database operation on IO thread
scope.launch {
    val result = withContext(Dispatchers.IO) {
        dao.operation()  // Runs on IO thread
    }
    // Result back on main thread for UI updates
    if (result > 0) {
        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
    }
}
```

---

These snippets can be copy-pasted and adapted for your specific needs!


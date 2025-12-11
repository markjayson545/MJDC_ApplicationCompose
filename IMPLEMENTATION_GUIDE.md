# Implementation Guide - Management Screens

## Quick Start

### What Changed?
All three management screens (Course, Employee, Product) now feature:
- 🎯 **FAB Button** - Click to add new items
- 📋 **BottomSheet Forms** - Modern, space-efficient input
- 📑 **LazyColumn Lists** - Efficient display of items
- ✏️ **Edit Mode** - Click edit button to modify items
- 🗑️ **Delete Confirmation** - Confirm before deleting

---

## Screen-by-Screen Guide

### 1. Course Management Screen

**File**: `CourseManagement.kt`

#### Features
- Add courses with name and code
- Edit existing courses
- Delete courses with confirmation
- View all courses in a list

#### How to Use
1. **Add Course**
   - Click the blue `+` button (FAB) at bottom-right
   - Fill in Course Name and Course Code
   - Click Save
   
2. **Edit Course**
   - Click the pencil (✏️) icon on any course
   - BottomSheet opens with current data
   - Modify and click Update
   
3. **Delete Course**
   - Click the trash (🗑️) icon on any course
   - Confirm in the dialog

#### Database Schema
```
Columns: course_id, first_name, course_code
Example: CRS-1, "Mobile App Dev", "MD101"
```

### 2. Employee Management Screen

**File**: `EmployeeManagement.kt`

#### Features
- Add employees with full credentials
- Edit employee information
- Delete employees with confirmation
- View all employees in a list

#### How to Use
1. **Add Employee**
   - Click the blue `+` button
   - Fill in all 5 fields:
     - First Name
     - Middle Name
     - Last Name
     - Username
     - Password (masked)
   - Click Save
   
2. **Edit Employee**
   - Click the pencil (✏️) icon on any employee
   - Modify any field
   - Click Update
   
3. **Delete Employee**
   - Click the trash (🗑️) icon
   - Confirm deletion

#### Card Display
Shows: Employee Name (First + Last)
       Username
       Middle Name

#### Database Schema
```
Columns: employee_id, first_name, middle_name, last_name, username, password
Example: EMP-1, "Mark", "Vinluan", "Dela Cruz", "markjayson545", "password123"
```

### 3. Product Management Screen

**File**: `ProductManagementActivity.kt`

#### Features
- Add products with name and description
- Edit product information
- Delete products with confirmation
- View all products in a list

#### How to Use
1. **Add Product**
   - Click the blue `+` button
   - Fill in:
     - Product Name
     - Product Description
   - Click Save
   
2. **Edit Product**
   - Click the pencil (✏️) icon
   - Modify details
   - Click Update
   
3. **Delete Product**
   - Click the trash (🗑️) icon
   - Confirm in dialog

#### Card Display
Shows: Product Name
       Product Description (up to 2 lines)

---

## Common Components Used

### 1. ManagementItemCard
Located in: `ManagementComponents.kt`

```kotlin
ManagementItemCard(
    itemNumber = (index + 1).toString(),  // "1", "2", etc
    title = item.name,                    // Main text
    subtitle = item.code,                 // Secondary text (optional)
    description = item.description,       // Details (optional)
    onEdit = { /* handle edit */ },
    onDelete = { /* handle delete */ }
)
```

### 2. InputBottomSheet
Displays a modal form for adding/editing items

```kotlin
InputBottomSheet(
    isVisible = showBottomSheet,
    title = "Add New Course",
    isEditMode = isEditMode,
    onDismiss = { showBottomSheet = false },
    onSaveClick = { saveItem() }
) {
    // Your form content here
}
```

### 3. DeleteConfirmationDialog
Shows confirmation before deletion

```kotlin
DeleteConfirmationDialog(
    isVisible = showDeleteDialog,
    title = "Delete Course",
    message = "Are you sure?",
    onConfirm = { deleteItem() },
    onDismiss = { showDeleteDialog = false }
)
```

---

## State Management Pattern

Each screen follows this pattern:

```kotlin
// 1. Input fields
var itemName by remember { mutableStateOf("") }
var itemCode by remember { mutableStateOf("") }

// 2. UI state
var showBottomSheet by remember { mutableStateOf(false) }
var isEditMode by remember { mutableStateOf(false) }

// 3. List of items
val items = remember { mutableStateListOf<Item>() }

// 4. Delete state
var showDeleteDialog by remember { mutableStateOf(false) }
var itemToDelete by remember { mutableStateOf("") }

// 5. Load data on start
LaunchedEffect(Unit) {
    val list = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }
    items.addAll(list)
}

// 6. Save function
fun saveItem() {
    if (isEditMode) {
        // Update
        dao.updateItem(itemId, newData)
    } else {
        // Insert
        dao.insertItem(newData)
    }
    resetForm()
    showBottomSheet = false
}

// 7. Reset function
fun resetForm() {
    itemName = ""
    itemCode = ""
    isEditMode = false
}
```

---

## Database Operations

### CourseDao Methods
```kotlin
// Create
insertCourse(courseName: String, courseCode: String): Long

// Read
getAllCourses(): List<Course>
getCourseById(courseId: String): Course?

// Update
updateCourse(courseId: String, courseName: String, courseCode: String): Int

// Delete
deleteCourse(courseId: String): Int
```

### EmployeeDao Methods
```kotlin
// Create
insertEmployee(firstName, middleName, lastName, username, password): Long

// Read
getAllEmployees(): List<Employee>
getEmployeeById(employeeId: String): Employee?

// Update
updateEmployee(id, firstName, middleName, lastName, username, password): Int

// Delete
deleteEmployee(id: String): Int
```

### ProductDao Methods
```kotlin
// Create
insertProduct(name: String, description: String): Long

// Read
getAllProducts(): List<Product>

// Update
updateProduct(id: Long, name: String, description: String): Int

// Delete
deleteProduct(id: String): Int
```

---

## Styling & Themes

### Colors Used
- **Primary**: Item number circle background
- **OnPrimary**: Text inside circle
- **PrimaryContainer**: Card background (with transparency)
- **OnSurface**: Main text
- **OnSurfaceVariant**: Secondary text
- **Red**: Delete button

### Typography
- **headlineSmall**: BottomSheet title
- **titleMedium**: Item title
- **bodySmall**: Item subtitle/description
- **bodyLarge**: Item number in circle

### Spacing
- 8dp: Standard margin between items
- 12dp: Item card padding
- 16dp: Screen padding
- 4dp: Small spacing between title/subtitle

---

## Error Handling

### Form Validation
All screens validate that required fields are not empty:

```kotlin
if (courseName.isNotEmpty() && courseCode.isNotEmpty()) {
    // Proceed with save
} else {
    Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
}
```

### Database Error Handling
```kotlin
if (status < 0) {
    // Insert/update failed
    Toast.makeText(context, "Failed to save item.", Toast.LENGTH_SHORT).show()
} else {
    // Success - reload list
    Toast.makeText(context, "Item saved successfully.", Toast.LENGTH_SHORT).show()
}
```

### Coroutine Safety
All database operations run on IO thread:
```kotlin
scope.launch {
    val result = withContext(Dispatchers.IO) {
        dao.operation()
    }
    // UI update happens on main thread
}
```

---

## Navigation

### Screen Entry Points
All screens are navigated to from a parent screen:

```kotlin
// Navigate TO management screen
NavController.navigate("courseManagement")

// Navigate BACK from management screen
NavController.popBackStack()
```

The back button in the top bar (◄) calls `popBackStack()`

---

## Customization Guide

### Change Item Card Title
In `ManagementItemCard`, modify how the title is displayed:

```kotlin
Text(
    title,
    style = MaterialTheme.typography.titleMedium
    // Customize: color, fontSize, fontWeight, etc.
)
```

### Add Fields to Forms
1. Add state variable: `var newField by remember { mutableStateOf("") }`
2. Add TextField in BottomSheet content
3. Include in save function
4. Add to model class

### Change BottomSheet Appearance
In `InputBottomSheet`:
```kotlin
ModalBottomSheet(
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    // Customize: backgroundColor, scrimColor, windowInsets, etc.
)
```

### Add More List Columns
Change `LazyColumn` to `LazyVerticalGrid`:
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),  // 2 columns
    modifier = Modifier.padding(8.dp)
) {
    // Items here
}
```

---

## Troubleshooting

### Items Not Showing
**Problem**: List appears empty
**Solution**: 
- Ensure database has data
- Check `LaunchedEffect` is loading data
- Verify DAO `getAll()` method

### Edit Not Working
**Problem**: Edit button doesn't open form
**Solution**:
- Check `isEditMode` flag is set
- Verify form fields are populated
- Ensure `showBottomSheet` is true

### Delete Not Working
**Problem**: Delete button doesn't show dialog
**Solution**:
- Check `showDeleteDialog` is toggled
- Verify `DeleteConfirmationDialog` is visible
- Check `itemToDelete` is populated

### BottomSheet Not Closing
**Problem**: BottomSheet stays open after save
**Solution**:
- Check `showBottomSheet = false` is called
- Verify save function completes
- Check no exceptions are thrown

### Form Data Lost
**Problem**: Data resets unexpectedly
**Solution**:
- Check `resetForm()` isn't called prematurely
- Verify edit mode setup
- Check LaunchedEffect dependencies

---

## Performance Tips

1. **Use LazyColumn** for large lists (built-in)
2. **Avoid nested Columns** in list items (optimized)
3. **Run DB ops on IO thread** (already done with Dispatchers.IO)
4. **Cache DAO instances** or pass as parameter
5. **Use mutableStateListOf** for efficient updates

---

## Testing Checklist

- [ ] Click FAB → BottomSheet opens
- [ ] Fill form and save → Item appears in list
- [ ] Click edit → Form pre-fills with data
- [ ] Click update → List refreshes
- [ ] Click delete → Confirmation appears
- [ ] Confirm delete → Item removed
- [ ] Close BottomSheet → Form data clears
- [ ] Click back button → Navigate to previous screen
- [ ] Empty list → Shows helpful message
- [ ] Network error → Shows error toast

---

## File Structure

```
/frontend/
  ├── ManagementComponents.kt      ← Reusable components
  ├── CourseManagement.kt          ← Course screen
  ├── EmployeeManagement.kt        ← Employee screen
  └── ProductManagementActivity.kt ← Product screen

/backend/
  ├── dao/
  │   ├── CourseDao.kt             ← Course database
  │   ├── EmployeeDao.kt           ← Employee database
  │   └── ProductDao.kt            ← Product database
  └── model/
      ├── Course.kt
      ├── Employee.kt
      └── Product.kt
```

---

## Additional Resources

- Material Design 3: https://m3.material.io/
- Jetpack Compose: https://developer.android.com/jetpack/compose
- BottomSheet: https://developer.android.com/reference/kotlin/androidx/compose/material3/ModalBottomSheet
- FAB: https://developer.android.com/reference/kotlin/androidx/compose/material3/FloatingActionButton


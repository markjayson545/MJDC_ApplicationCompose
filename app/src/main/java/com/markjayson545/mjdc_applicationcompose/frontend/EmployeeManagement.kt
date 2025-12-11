package com.markjayson545.mjdc_applicationcompose.frontend

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceDbHelper
import com.markjayson545.mjdc_applicationcompose.backend.dao.EmployeeDao
import com.markjayson545.mjdc_applicationcompose.backend.model.Employee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagement(navController: NavController) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingEmployeeId by remember { mutableStateOf("") }

    val employees = remember { mutableStateListOf<Employee>() }
    val dbHelper = AttendanceDbHelper(context)
    val employeeDao = EmployeeDao(dbHelper)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf("") }

    // Load employees on initial composition
    LaunchedEffect(Unit) {
        coroutine.launch {
            val employeesList = withContext(Dispatchers.IO) {
                employeeDao.getAllEmployees()
            }
            employees.clear()
            employees.addAll(employeesList)
        }
    }

    fun resetForm() {
        firstName = ""
        middleName = ""
        lastName = ""
        username = ""
        password = ""
        isEditMode = false
        editingEmployeeId = ""
    }

    fun saveEmployee() {
        if (firstName.isNotEmpty() && middleName.isNotEmpty() && lastName.isNotEmpty() &&
            username.isNotEmpty() && password.isNotEmpty()
        ) {
            coroutine.launch {
                if (isEditMode) {
                    val result = withContext(Dispatchers.IO) {
                        employeeDao.updateEmployee(
                            editingEmployeeId, firstName, middleName, lastName, username, password
                        )
                    }
                    if (result > 0) {
                        Toast.makeText(context, "Teacher updated successfully!", Toast.LENGTH_LONG)
                            .show()
                        val updatedEmployees = withContext(Dispatchers.IO) {
                            employeeDao.getAllEmployees()
                        }
                        employees.clear()
                        employees.addAll(updatedEmployees)
                    } else {
                        Toast.makeText(context, "Failed to update teacher.", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    val status = withContext(Dispatchers.IO) {
                        employeeDao.insertEmployee(
                            firstName,
                            middleName,
                            lastName,
                            username,
                            password
                        )
                    }
                    if (status < 0) {
                        Toast.makeText(
                            context,
                            "Failed to save teacher. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(context, "Teacher saved successfully!", Toast.LENGTH_LONG)
                            .show()
                        val updatedEmployees = withContext(Dispatchers.IO) {
                            employeeDao.getAllEmployees()
                        }
                        employees.clear()
                        employees.addAll(updatedEmployees)
                    }
                }
                resetForm()
                showBottomSheet = false
            }
        } else {
            Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    resetForm()
                    showBottomSheet = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Teacher")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (employees.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No teacher yet. Click the + button to add one!")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    itemsIndexed(employees) { index, employee ->
                        ManagementItemCard(
                            itemNumber = (index + 1).toString(),
                            title = "${employee.firstName} ${employee.middleName} ${employee.lastName}",
                            subtitle = employee.username,
                            description = employee.password.replaceRange(
                                1,
                                employee.password.length - 1,
                                "*".repeat(employee.password.length - 2)
                            ),
                            onEdit = {
                                firstName = employee.firstName
                                middleName = employee.middleName
                                lastName = employee.lastName
                                username = employee.username
                                password = employee.password
                                editingEmployeeId = employee.employeeId
                                isEditMode = true
                                showBottomSheet = true
                            },
                            onDelete = {
                                employeeToDelete = employee.employeeId
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for adding/editing
    InputBottomSheet(
        isVisible = showBottomSheet,
        title = if (isEditMode) "Edit Teacher" else "Add New Teacher",
        isEditMode = isEditMode,
        onDismiss = {
            showBottomSheet = false
            resetForm()
        },
        onSaveClick = { saveEmployee() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ManagementTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                placeholder = "e.g., Mark",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = middleName,
                onValueChange = { middleName = it },
                label = "Middle Name",
                placeholder = "e.g., Vinluan",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Last Name",
                placeholder = "e.g., Dela Cruz",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                placeholder = "e.g., markjayson545",
                icon = Icons.Default.VerifiedUser
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                icon = Icons.Default.Password,
                isPassword = true
            )
        }
    }

    // Delete confirmation dialog
    DeleteConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Employee",
        message = "Are you sure you want to delete this teacher?",
        onConfirm = {
            coroutine.launch {
                val result = withContext(Dispatchers.IO) {
                    employeeDao.deleteEmployee(employeeToDelete)
                }
                if (result > 0) {
                    Toast.makeText(context, "Teacher deleted successfully.", Toast.LENGTH_SHORT)
                        .show()
                    val updatedEmployees = withContext(Dispatchers.IO) {
                        employeeDao.getAllEmployees()
                    }
                    employees.clear()
                    employees.addAll(updatedEmployees)
                } else {
                    Toast.makeText(context, "Failed to delete teacher.", Toast.LENGTH_SHORT).show()
                }
                showDeleteDialog = false
            }
        },
        onDismiss = {
            showDeleteDialog = false
            employeeToDelete = ""
        }
    )
}
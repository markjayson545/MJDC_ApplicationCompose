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
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.TextFields
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
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.backend.DatabaseHelper
import com.markjayson545.mjdc_applicationcompose.backend.dao.ProductDao
import com.markjayson545.mjdc_applicationcompose.backend.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(navController: NavController = rememberNavController()) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingProductId by remember { mutableStateOf("") }

    val dbHelper = DatabaseHelper(context)
    val productDao = ProductDao(dbHelper)
    val products = remember { mutableStateListOf<Product>() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutine.launch {
            val list = withContext(Dispatchers.IO) {
                productDao.getAllProducts()
            }
            products.addAll(list)
        }
    }

    fun resetForm() {
        productName = ""
        productDescription = ""
        isEditMode = false
        editingProductId = ""
    }

    fun saveProduct() {
        if (productName.isNotEmpty() && productDescription.isNotEmpty()) {
            coroutine.launch {
                if (isEditMode) {
                    val result = withContext(Dispatchers.IO) {
                        productDao.updateProduct(
                            id = editingProductId,
                            name = productName,
                            description = productDescription
                        )
                    }
                    if (result > 0) {
                        Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT)
                            .show()
                        val list = withContext(Dispatchers.IO) {
                            productDao.getAllProducts()
                        }
                        products.clear()
                        products.addAll(list)
                    } else {
                        Toast.makeText(context, "Failed to update product.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    val status = withContext(Dispatchers.IO) {
                        productDao.insertProduct(productName, productDescription)
                    }
                    if (status != -1L) {
                        Toast.makeText(context, "Product saved successfully!", Toast.LENGTH_SHORT)
                            .show()
                        val list = withContext(Dispatchers.IO) {
                            productDao.getAllProducts()
                        }
                        products.clear()
                        products.addAll(list)
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to save product. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        val updatedProducts = withContext(Dispatchers.IO) {
                            productDao.getAllProducts()
                        }
                        products.clear()
                        products.addAll(updatedProducts)
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
                title = { Text("Product Management") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (products.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No products yet. Click the + button to add one!")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
                    itemsIndexed(products) { index, product ->
                        ManagementItemCard(
                            itemNumber = (index + 1).toString(),
                            title = product.name,
                            description = product.description,
                            onEdit = {
                                productName = product.name
                                productDescription = product.description
                                editingProductId = product.id
                                isEditMode = true
                                showBottomSheet = true
                            },
                            onDelete = {
                                productToDelete = product.id
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
        title = if (isEditMode) "Edit Product" else "Add New Product",
        isEditMode = isEditMode,
        onDismiss = {
            showBottomSheet = false
            resetForm()
        },
        onSaveClick = { saveProduct() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ManagementTextField(
                value = productName,
                onValueChange = { productName = it },
                label = "Product Name",
                placeholder = "e.g., Product Name",
                icon = Icons.Default.TextFields
            )
            Spacer(modifier = Modifier.height(12.dp))
            ManagementTextField(
                value = productDescription,
                onValueChange = { productDescription = it },
                label = "Product Description",
                placeholder = "e.g., Product Description",
                icon = Icons.Default.Preview
            )
        }
    }

    // Delete confirmation dialog
    DeleteConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Product",
        message = "Are you sure you want to delete this product?",
        onConfirm = {
            coroutine.launch {
                withContext(Dispatchers.IO) {
                    productDao.deleteProduct(productToDelete)
                }
                Toast.makeText(context, "Product deleted successfully.", Toast.LENGTH_SHORT).show()
                val list = withContext(Dispatchers.IO) {
                    productDao.getAllProducts()
                }
                products.clear()
                products.addAll(list)
                showDeleteDialog = false
            }
        },
        onDismiss = {
            showDeleteDialog = false
            productToDelete = ""
        }
    )
}

package com.markjayson545.mjdc_applicationcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.frontend.AttendanceManagementActivity
import com.markjayson545.mjdc_applicationcompose.frontend.CalculatorScreen
import com.markjayson545.mjdc_applicationcompose.frontend.CartScreen
import com.markjayson545.mjdc_applicationcompose.frontend.CheckoutScreen
import com.markjayson545.mjdc_applicationcompose.frontend.CourseManagement
import com.markjayson545.mjdc_applicationcompose.frontend.EmployeeManagement
import com.markjayson545.mjdc_applicationcompose.frontend.ItemDetails
import com.markjayson545.mjdc_applicationcompose.frontend.LoginScreen
import com.markjayson545.mjdc_applicationcompose.frontend.ProductManagementScreen
import com.markjayson545.mjdc_applicationcompose.frontend.WebviewLocalHostScreen
import com.markjayson545.mjdc_applicationcompose.frontend.WebviewScreen
import com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator.AttendanceNavigator
import com.markjayson545.mjdc_applicationcompose.frontend.navigator.AppNavigation
import com.markjayson545.mjdc_applicationcompose.backend.AttendanceSystemDatabase
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels
import com.markjayson545.mjdc_applicationcompose.ui.theme.MJDC_ApplicationComposeTheme
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val database by lazy { AttendanceSystemDatabase.getInstance(this) }
    private val sharedViewModels by lazy { SharedViewModels(database) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MJDC_ApplicationComposeTheme {
                MainApp(sharedViewModels)
            }
        }
    }

    data class screen(
        val onClick: () -> Unit,
        val icon: ImageVector,
        val title: String,
        val description: String,
        val iconColor: Color = Color(0xFF6650a4)
    )

    @Composable
    fun MainApp(sharedViewModels: SharedViewModels) {
        val navController = rememberNavController()
        NavHost(
            navController = navController, startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("login") {
                LoginScreen(navController)
            }
            composable("calculator") {
                CalculatorScreen(navController)
            }
            composable("cart/{username}") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username")
                CartScreen(navController, username ?: "")
            }
            composable("checkout/{productsJson}") { backStackEntry ->
                val productsJson = backStackEntry.arguments?.getString("productsJson") ?: ""
                val decodedJson = URLDecoder.decode(productsJson, StandardCharsets.UTF_8.toString())
                val products = try {
                    Json.decodeFromString<List<ItemDetails>>(decodedJson)
                } catch (e: Exception) {
                    emptyList()
                }
                CheckoutScreen(navController, products)
            }
            composable("webview") {
                WebviewScreen(navController)
            }
            composable("webviewLocalhost") {
                WebviewLocalHostScreen(navController)
            }
            composable("navigation") {
                AppNavigation()
            }
            composable("productManagement") {
                ProductManagementScreen(navController)
            }
            composable("attendanceManagement") {
                AttendanceManagementActivity(navController)
            }
            composable("employeeManagement") {
                EmployeeManagement(navController)
            }
            composable("courseManagement") {
                CourseManagement(navController)
            }
            composable("attendanceSystem") {
                AttendanceNavigator(sharedViewModels)
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {

        val screens = listOf<screen>(
            screen(
                onClick = { navController.navigate("attendanceSystem") },
                icon = Icons.Default.Person,
                title = "CheckIns Monitoring System",
                description = "Final project in MD101",
                iconColor = Color(0xFF4CAF50)
            ),
            screen(
                onClick = { navController.navigate("login") },
                icon = Icons.Default.VerifiedUser,
                title = "Login",
                description = "Example login activity for the application",
                iconColor = Color(0xFF2196F3)
            ),
            screen(
                onClick = { navController.navigate("calculator") },
                icon = Icons.Default.Calculate,
                title = "Calculator",
                description = "Example Calculator",
                iconColor = Color(0xFFFF9800)
            ),
            screen(
                onClick = { navController.navigate("webview") },
                icon = Icons.Default.Web,
                title = "Webview",
                description = "Example Webview for browsing the internet",
                iconColor = Color(0xFF9C27B0)
            ),
            screen(
                onClick = { navController.navigate("webviewLocalhost") },
                icon = Icons.Default.WebAsset,
                title = "Webview Localhost",
                description = "Example webview to simulate web app",
                iconColor = Color(0xFF673AB7)
            ),
            screen(
                onClick = { navController.navigate("navigation") },
                icon = Icons.Default.Navigation,
                title = "Navigation",
                description = "Bottom Navigation bar example",
                iconColor = Color(0xFF00BCD4)
            ),
            screen(
                onClick = { navController.navigate("productManagement") },
                icon = Icons.Default.ShoppingCart,
                title = "Product Management",
                description = "Example product management",
                iconColor = Color(0xFFE91E63)
            ),
            screen(
                onClick = { navController.navigate("attendanceManagement") },
                icon = Icons.Default.Person,
                title = "Student Management",
                description = "Example attendance management",
                iconColor = Color(0xFF3F51B5)
            ),
            screen(
                onClick = { navController.navigate("employeeManagement") },
                icon = Icons.Default.Person,
                title = "Teacher Management",
                description = "Example teacher management",
                iconColor = Color(0xFF009688)
            ),
            screen(
                onClick = { navController.navigate("courseManagement") },
                icon = Icons.Default.School,
                title = "Course Management",
                description = "Example course management",
                iconColor = Color(0xFFFF5722)
            )
        )


        Scaffold(Modifier.fillMaxSize()) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header Section with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "MJ",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Mark Jayson V. Dela Cruz",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "BSIT-3B",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Bachelor of Science in Information Technology",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }

                // Menu Section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Menu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(screens) { screen ->
                            ScreenItem(
                                onClick = screen.onClick,
                                icon = screen.icon,
                                title = screen.title,
                                description = screen.description,
                                iconColor = screen.iconColor
                            )
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ScreenItem(
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {},
        icon: ImageVector = Icons.Default.VerifiedUser,
        title: String = "Login",
        description: String = "Example login activity for the application",
        iconColor: Color = MaterialTheme.colorScheme.primary
    ) {
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            label = "scale"
        )

        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with colored background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
package com.markjayson545.mjdc_applicationcompose.frontend.navigator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.markjayson545.mjdc_applicationcompose.frontend.WebviewLocalHostScreen
import com.markjayson545.mjdc_applicationcompose.frontend.WebviewScreen

@Composable
fun AppNavigation() {
    var selectedDestination by remember { mutableIntStateOf(NavigationDestinations.ONLINE.ordinal) }
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationDestinations.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            selectedDestination = index
                            navController.navigate(destination.route) {
                                // Pop up to the start destination to avoid building up a back stack
                                popUpTo(NavigationDestinations.ONLINE.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when reselecting
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(text = destination.label)
                        },
                        modifier = Modifier.testTag("${destination.label}TabButton")
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationDestinations.ONLINE.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationDestinations.ONLINE.route) {
                WebviewScreen(navController)
            }
            composable(NavigationDestinations.LOCALHOST.route) {
                WebviewLocalHostScreen(navController)
            }
        }
    }
}
package com.markjayson545.mjdc_applicationcompose.frontend.navigator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Web
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    ONLINE("Online", Icons.Default.Web, "online"),
    LOCALHOST("Localhost", Icons.Default.Web, "localhost"),
}

package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.navigator

import androidx.compose.runtime.Composable
import com.markjayson545.mjdc_applicationcompose.bridge.SharedViewModels

/**
 * Legacy navigator - deprecated in favor of AttendanceNavHost
 * This wrapper is kept for backward compatibility
 */
@Composable
fun AttendanceNavigator(sharedViewModels: SharedViewModels) {
    AttendanceNavHost(sharedViewModels = sharedViewModels)
}
/**
 * ============================================================================
 * ANIMATED NUMBER UTILITIES
 * ============================================================================
 *
 * This file provides animated counter composables for smooth number transitions.
 * Numbers animate from their current value to a new target value using
 * Material 3 Expressive motion scheme.
 *
 * ANIMATION BEHAVIOR:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ When targetValue changes:                                               │
 * │                                                                         │
 * │  Old Value ────────────────────────────────► New Value                 │
 * │             ↑                                                           │
 * │     Smooth animation using fastSpatialSpec()                           │
 * │     Duration: ~300ms with bouncy easing                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * AVAILABLE COMPOSABLES:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ AnimatedIntCounter                                                      │
 * │ - Animates integer values                                              │
 * │ - Supports prefix/suffix (e.g., "$100", "50%")                         │
 * │ - Use for: Student counts, attendance rates, totals                    │
 * │                                                                         │
 * │ AnimatedFloatCounter                                                    │
 * │ - Animates float values with decimal places                            │
 * │ - Configurable decimal precision                                        │
 * │ - Use for: Percentages, averages, calculated values                    │
 * │                                                                         │
 * │ AnimatedIntCounterOrNA                                                  │
 * │ - Shows "N/A" when showValue is false                                  │
 * │ - Use for: Conditional display (e.g., rate when no students)           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * USAGE EXAMPLES:
 * ```kotlin
 * // Simple counter
 * AnimatedIntCounter(
 *     targetValue = studentCount,
 *     style = MaterialTheme.typography.headlineLarge
 * )
 *
 * // Counter with suffix
 * AnimatedIntCounter(
 *     targetValue = attendanceRate,
 *     suffix = "%",
 *     color = when {
 *         attendanceRate >= 80 -> Color.Green
 *         else -> Color.Red
 *     }
 * )
 *
 * // Float counter with decimals
 * AnimatedFloatCounter(
 *     targetValue = averageScore,
 *     decimalPlaces = 2,
 *     prefix = "Avg: "
 * )
 * ```
 *
 * USED IN COMPONENTS:
 * - CompactStatsCard: Animated value display
 * - ReportsScreen: Statistics display
 * - AttendanceDashboard: Overview metrics
 *
 * NOTE: Requires @OptIn(ExperimentalMaterial3ExpressiveApi::class) annotation
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.text.DecimalFormat

/**
 * Animated counter composables for smooth number transitions
 * using Material Expressive motion scheme.
 */

/**
 * Animated integer counter that smoothly transitions between values.
 * Uses Material Expressive fast spatial spec for bouncy animations.
 *
 * @param targetValue The target integer value to animate to
 * @param modifier Modifier for the Text composable
 * @param style TextStyle for the counter text
 * @param fontWeight Font weight for the counter text
 * @param color Text color
 * @param prefix Optional prefix string (e.g., "$")
 * @param suffix Optional suffix string (e.g., "%", " students")
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedIntCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    prefix: String = "",
    suffix: String = ""
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "intCounter"
    )

    Text(
        text = "$prefix$animatedValue$suffix",
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        color = color
    )
}

/**
 * Animated float counter that smoothly transitions between values.
 * Uses Material Expressive fast spatial spec for bouncy animations.
 *
 * @param targetValue The target float value to animate to
 * @param modifier Modifier for the Text composable
 * @param style TextStyle for the counter text
 * @param fontWeight Font weight for the counter text
 * @param color Text color
 * @param decimalPlaces Number of decimal places to show
 * @param prefix Optional prefix string
 * @param suffix Optional suffix string
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedFloatCounter(
    targetValue: Float,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    decimalPlaces: Int = 1,
    prefix: String = "",
    suffix: String = ""
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "floatCounter"
    )

    val pattern = if (decimalPlaces > 0) {
        "#." + "0".repeat(decimalPlaces)
    } else {
        "#"
    }
    val decimalFormat = DecimalFormat(pattern)

    Text(
        text = "$prefix${decimalFormat.format(animatedValue)}$suffix",
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        color = color
    )
}

/**
 * Animated percentage counter with smooth transitions.
 * Automatically appends "%" suffix.
 *
 * @param targetValue The target percentage value (0-100)
 * @param modifier Modifier for the Text composable
 * @param style TextStyle for the counter text
 * @param fontWeight Font weight for the counter text
 * @param color Text color
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedPercentageCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue.coerceIn(0, 100),
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "percentageCounter"
    )

    Text(
        text = "$animatedValue%",
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        color = color
    )
}

/**
 * Animated counter with N/A fallback for invalid data.
 * Shows "N/A" when value is null or condition is not met.
 *
 * @param targetValue The target integer value, or null for N/A
 * @param showValue Condition to show the actual value (defaults to value != null)
 * @param modifier Modifier for the Text composable
 * @param style TextStyle for the counter text
 * @param fontWeight Font weight for the counter text
 * @param color Text color
 * @param naText Text to show when value is not available
 * @param suffix Optional suffix string
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedIntCounterOrNA(
    targetValue: Int?,
    showValue: Boolean = targetValue != null,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    naText: String = "N/A",
    suffix: String = ""
) {
    if (showValue && targetValue != null) {
        val animatedValue by animateIntAsState(
            targetValue = targetValue,
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
            label = "intCounterOrNA"
        )

        Text(
            text = "$animatedValue$suffix",
            modifier = modifier,
            style = style,
            fontWeight = fontWeight,
            color = color
        )
    } else {
        Text(
            text = naText,
            modifier = modifier,
            style = style,
            fontWeight = fontWeight,
            color = color.copy(alpha = 0.6f)
        )
    }
}


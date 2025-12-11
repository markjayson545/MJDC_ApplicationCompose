/**
 * ============================================================================
 * MOTION UTILITIES
 * ============================================================================
 *
 * This file provides animation utilities using Material 3 Expressive motion
 * scheme. These utilities create consistent, bouncy animations across the
 * attendance system.
 *
 * MATERIAL 3 EXPRESSIVE MOTION:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Material 3 Expressive introduces a new motion system with:             │
 * │                                                                         │
 * │ SPATIAL ANIMATIONS (for position/scale changes):                        │
 * │ - fastSpatialSpec(): Quick, bouncy - button presses, card animations   │
 * │ - defaultSpatialSpec(): Standard - most transitions                    │
 * │ - slowSpatialSpec(): Dramatic - page transitions, large movements      │
 * │                                                                         │
 * │ EFFECTS ANIMATIONS (for visual property changes):                       │
 * │ - fastEffectsSpec(): Quick - color changes, opacity                    │
 * │ - defaultEffectsSpec(): Standard - most visual effects                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * PROVIDED TRANSITIONS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ bouncyEnterTransition(index)                                           │
 * │ - Combines: fadeIn + slideInVertically + scaleIn                       │
 * │ - Use for: List items appearing with stagger                           │
 * │                                                                         │
 * │ bouncyExitTransition()                                                 │
 * │ - Combines: fadeOut + slideOutVertically + scaleOut                    │
 * │ - Use for: List items disappearing                                     │
 * │                                                                         │
 * │ slideInFromEnd() / slideOutToStart()                                   │
 * │ - Horizontal slide with bounce                                         │
 * │ - Use for: Screen transitions, drawer content                          │
 * │                                                                         │
 * │ popInTransition() / popOutTransition()                                 │
 * │ - Scale animation for appearing/disappearing elements                  │
 * │ - Use for: FABs, dialog content, tooltips                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * STAGGER ANIMATION:
 * - MotionUtils.STAGGER_DELAY_MS: Default 50ms between items
 * - MotionUtils.staggerDelay(index): Calculate delay for item at index
 * - Use with AnimatedVisibility for list item entrance effects
 *
 * USAGE EXAMPLE:
 * ```kotlin
 * itemsIndexed(list) { index, item ->
 *     AnimatedVisibility(
 *         visible = showContent,
 *         enter = bouncyEnterTransition(index = index)
 *     ) {
 *         ItemCard(item)
 *     }
 * }
 * ```
 *
 * NOTE: Requires @OptIn(ExperimentalMaterial3ExpressiveApi::class) annotation
 */
package com.markjayson545.mjdc_applicationcompose.frontend.attendance_system.utils

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Motion utilities using Material 3 Expressive motion scheme.
 * Provides consistent bouncy animations across the attendance system.
 */
object MotionUtils {

    /**
     * Default stagger delay for list item animations (ms)
     */
    const val STAGGER_DELAY_MS = 50L

    /**
     * Calculate stagger delay for indexed items
     */
    fun staggerDelay(index: Int, baseDelay: Long = STAGGER_DELAY_MS): Long {
        return index * baseDelay
    }
}

/**
 * Get the fast spatial animation spec from Material Expressive theme.
 * Used for quick, bouncy interactions like button presses and card animations.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> {
    return MaterialTheme.motionScheme.fastSpatialSpec()
}

/**
 * Get the slow spatial animation spec from Material Expressive theme.
 * Used for larger, more dramatic animations like page transitions.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> {
    return MaterialTheme.motionScheme.slowSpatialSpec()
}

/**
 * Get the default spatial animation spec from Material Expressive theme.
 * Used for standard animations.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> {
    return MaterialTheme.motionScheme.defaultSpatialSpec()
}

/**
 * Get the fast effects animation spec from Material Expressive theme.
 * Used for quick visual feedback like color changes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> {
    return MaterialTheme.motionScheme.fastEffectsSpec()
}

/**
 * Get the default effects animation spec from Material Expressive theme.
 * Used for standard visual effects.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> {
    return MaterialTheme.motionScheme.defaultEffectsSpec()
}

/**
 * Bouncy enter transition for list items with staggered delay.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun bouncyEnterTransition(
    index: Int = 0,
    fromBottom: Boolean = true
): EnterTransition {
    val direction = if (fromBottom) 1 else -1
    return fadeIn(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    ) + slideInVertically(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        initialOffsetY = { fullHeight -> direction * (fullHeight / 4) }
    ) + scaleIn(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        initialScale = 0.92f
    )
}

/**
 * Bouncy exit transition for list items.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun bouncyExitTransition(
    toBottom: Boolean = true
): ExitTransition {
    val direction = if (toBottom) 1 else -1
    return fadeOut(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    ) + slideOutVertically(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        targetOffsetY = { fullHeight -> direction * (fullHeight / 4) }
    ) + scaleOut(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        targetScale = 0.92f
    )
}

/**
 * Horizontal slide enter transition with bounce.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun slideInFromEnd(): EnterTransition {
    return fadeIn(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    ) + slideInHorizontally(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        initialOffsetX = { fullWidth -> fullWidth / 3 }
    )
}

/**
 * Horizontal slide exit transition with bounce.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun slideOutToStart(): ExitTransition {
    return fadeOut(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    ) + slideOutHorizontally(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        targetOffsetX = { fullWidth -> -fullWidth / 3 }
    )
}

/**
 * Pop-in scale animation for appearing elements.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun popInTransition(): EnterTransition {
    return fadeIn(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    ) + scaleIn(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        initialScale = 0.8f
    )
}

/**
 * Pop-out scale animation for disappearing elements.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun popOutTransition(): ExitTransition {
    return fadeOut(
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
    ) + scaleOut(
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        targetScale = 0.8f
    )
}


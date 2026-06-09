package com.finflow.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private const val ANIM_MS = 180

fun enterFromRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = tween(ANIM_MS)) { it / 4 } + fadeIn(tween(ANIM_MS))
}
fun exitToLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = tween(ANIM_MS)) { -it / 4 } + fadeOut(tween(ANIM_MS))
}
fun popEnterFromLeft(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = tween(ANIM_MS)) { -it / 4 } + fadeIn(tween(ANIM_MS))
}
fun popExitToRight(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = tween(ANIM_MS)) { it / 4 } + fadeOut(tween(ANIM_MS))
}

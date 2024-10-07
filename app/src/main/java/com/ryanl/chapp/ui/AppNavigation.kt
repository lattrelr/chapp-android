package com.ryanl.chapp.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("users") {
            UsersScreen(navController = navController)
        }
        composable(
            "chat/{id}/{name}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
                navArgument("name") {
                    type = NavType.StringType
                }
            ),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        200, easing = LinearEasing
                    ),
                    initialAlpha = 1F
                ) + slideIntoContainer(
                    animationSpec = tween(200, easing = LinearEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    initialOffset = { it }
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    ),
                    targetAlpha = 0F
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = LinearEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    targetOffset = { -it/4 }
                )
            },
        ) { backStackEntry ->
            val arguments = requireNotNull(backStackEntry.arguments)
            val id = arguments.getString("id")
            val name = arguments.getString("name")
            ChatScreen(id, name)
        }
    }
}

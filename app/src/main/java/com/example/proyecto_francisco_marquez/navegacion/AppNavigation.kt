package com.example.proyecto_francisco_marquez.navegacion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.ui.screen.*
import com.example.proyecto_francisco_marquez.viewmodel.AuthViewModel
import com.example.proyecto_francisco_marquez.ui.screen.FilterScreen
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModelFactory

@Composable
fun AppNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Ruta de inicio
        composable("splash") {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Rutas de autenticación
        composable(
            route = "login",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
        ) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(
            route = "register",
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) }
        ) {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Rutas principales de la app (requieren autenticación)
        composable(
            route = "filter",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) {
            FilterScreen(navController)
        }


        composable("forgotPassword") {
            ForgotPasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("characterScreen/{status}") { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")
            CharacterScreen(
                navController = navController,
                filter = status ?: "all"
            )
        }
        composable("characterDetail/{characterId}") { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
            CharacterDetailScreen(characterId, navController)
        }
        composable("databaseScreen") {
            DatabaseScreen(navController)
        }

        composable("databasePersonajeScreen") {
            DatabaseScreenPersonaje(navController)
        }

        composable("databaseEpisodioScreen") {
            DatabaseScreenEpisodio(navController)
        }

        composable("agregarPersonajeScreen") {
            AgregarPersonajeScreen(navController)
        }

        composable("agregarEpisodioScreen") {
            AgregarEpisodioScreen(navController)
        }

        composable(
            "modificarPersonajeScreen/{characterId}",
            arguments = listOf(
                navArgument("characterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: return@composable
            ModificarPersonajeScreen(
                navController = navController,
                characterId = characterId
            )
        }

        composable(
            "modificarEpisodioScreen/{episodeId}",
            arguments = listOf(
                navArgument("episodeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString("episodeId") ?: return@composable
            ModificarEpisodioScreen(
                navController = navController,
                episodeId = episodeId
            )
        }

        composable(
            "verPersonajesEpisodio/{episodeId}",
            arguments = listOf(
                navArgument("episodeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString("episodeId") ?: return@composable
            val viewModel: DatabaseViewModel = viewModel(
                factory = DatabaseViewModelFactory(FirestoreService())
            )
            VerPersonajesEpisodioScreen(
                navController = navController,
                episodeId = episodeId,
                viewModel = viewModel
            )
        }
    }
}
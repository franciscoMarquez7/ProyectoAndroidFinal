package com.example.proyecto_francisco_marquez.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_francisco_marquez.ui.screen.*
import com.example.proyecto_francisco_marquez.viewmodel.AuthViewModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import com.example.proyecto_francisco_marquez.viewmodel.SharedViewModel
import com.example.proyecto_francisco_marquez.viewmodel.CharacterViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") { 
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            ) 
        }
        
        composable("login") { 
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            ) 
        }
        
        composable("register") {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable("forgotPassword") {
            ForgotPasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable("filter") { 
            FilterScreen(navController) 
        }
        
        composable("characterScreen/{status}") { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")
            CharacterScreen(
                navController = navController,
                filter = status ?: "all"
            )
        }
        
        composable("databaseScreen") { 
            DatabaseScreen(navController) 
        }
        
        composable("databasePersonajeScreen") { 
            DatabaseScreenPersonaje(navController = navController) 
        }
        
        composable("databaseEpisodioScreen") {
            DatabaseScreenEpisodio(navController, viewModel())
        }
        
        composable("agregarEpisodioScreen") {
            AgregarEpisodioScreen(navController, viewModel())
        }
        
        composable(
            "modificarEpisodioScreen/{episodeId}",
            arguments = listOf(navArgument("episodeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
            ModificarEpisodioScreen(navController, episodeId, viewModel())
        }
        
        composable(
            route = "verPersonajesEpisodio/{episodeId}",
            arguments = listOf(
                navArgument("episodeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString("episodeId") ?: return@composable
            VerPersonajesEpisodioScreen(
                navController = navController,
                episodeId = episodeId
            )
        }
    }
} 
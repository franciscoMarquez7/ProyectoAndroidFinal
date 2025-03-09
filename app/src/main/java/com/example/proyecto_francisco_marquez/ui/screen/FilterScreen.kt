package com.example.proyecto_francisco_marquez.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.viewmodel.AuthViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Pantalla de filtros que permite filtrar personajes por estado y acceder a la base de datos
 * @param navController Controlador de navegación
 */

@Composable
fun FilterScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val userState by authViewModel.userState.collectAsState()

    LaunchedEffect(userState) {
        if (userState == null) {
            try {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            } catch (e: Exception) {
                Log.e("FilterScreen", "Error en navegación: ${e.message}")
            }
        }
    }

    FilterScreenContent(
        onLogoutClick = { showLogoutDialog = true },
        onFilterClick = { status -> navController.navigate("characterScreen/$status") },
        onDatabaseClick = { navController.navigate("databaseScreen") }
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
/**
 * Contenido principal de la pantalla de filtros
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterScreenContent(
    onLogoutClick: () -> Unit,
    onFilterClick: (String) -> Unit,
    onDatabaseClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Menu",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FilterCard(onFilterClick = onFilterClick)
                Spacer(modifier = Modifier.height(24.dp))
                DatabaseButton(onClick = onDatabaseClick)
            }
        }
    }
}

@Composable
private fun BackgroundImage() {
    Image(
        painter = painterResource(id = R.drawable.imagen_fondo),
        contentDescription = "Fondo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = 0.3f
    )
}

@Composable
private fun FilterCard(onFilterClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilterTitle()
            FilterButtons(onFilterClick)
        }
    }
}

@Composable
private fun FilterTitle() {
    Text(
        "Filtrar por Estado",
        style = MaterialTheme.typography.titleLarge,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun FilterButtons(onFilterClick: (String) -> Unit) {
    val filterOptions = listOf(
        FilterOption("Vivo", "Alive", Color.Green),
        FilterOption("Muerto", "Dead", Color.Red),
        FilterOption("Desconocido", "Unknown", Color.Gray),
        FilterOption("Todos", "All", Color.Blue)
    )

    filterOptions.forEach { option ->
        FilterButton(
            text = option.displayText,
            color = option.color.copy(alpha = 0.8f),
            onClick = { onFilterClick(option.status) }
        )
    }
}

@Composable
private fun DatabaseButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Base de Datos", fontSize = 16.sp, color = Color.White)
    }
}

@Composable
private fun FilterButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class FilterOption(
    val displayText: String,
    val status: String,
    val color: Color
)
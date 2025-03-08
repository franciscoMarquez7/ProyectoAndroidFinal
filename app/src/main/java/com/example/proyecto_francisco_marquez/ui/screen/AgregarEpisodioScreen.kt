package com.example.proyecto_francisco_marquez.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.model.EpisodeModel
import com.example.proyecto_francisco_marquez.ui.TitleStyle
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarEpisodioScreen(navController: NavHostController) {
    val viewModel: DatabaseViewModel = viewModel(
        factory = DatabaseViewModelFactory(FirestoreService())
    )
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo con efecto de oscurecimiento
        Image(
            painter = painterResource(id = R.drawable.imagen_fondo),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Agregar Episodio", style = TitleStyle.copy(color = Color.Black)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campos de entrada
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del episodio") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha de emisión") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duración") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la imagen") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        scope.launch {
                            val episode = EpisodeModel(
                                name = name,
                                date = date,
                                duration = duration,
                                imageUrl = imageUrl
                            )

                            val success = viewModel.addEpisode(episode) // Ahora devuelve Boolean
                            if (success) {
                                navController.popBackStack()
                            } else {
                                println("Error al agregar episodio")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Agregar Episodio", color = Color.White)
                }
            }
        }
    }
}

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.ui.TitleStyle
import com.example.proyecto_francisco_marquez.data.FirestoreService
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_francisco_marquez.model.EpisodeModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarEpisodioScreen(navController: NavHostController, episodeId: String) {
    val viewModel: DatabaseViewModel = viewModel(
        factory = DatabaseViewModelFactory(FirestoreService())
    )

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(episodeId) {
        try {
            isLoading = true
            val episode = viewModel.getEpisodeById(episodeId)
            episode?.let {
                name = it.name
                date = it.date
                duration = it.duration
                imageUrl = it.imageUrl
            }
        } catch (e: Exception) {
            println("Error cargando episodio: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo con efecto de oscurecimiento
        Image(
            painter = painterResource(id = R.drawable.imagen_fondo),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("Modificar Episodio", style = TitleStyle.copy(color = Color.Black)) },
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
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre del episodio") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6200EE),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Fecha de emisión") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6200EE),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("Número de episodio") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6200EE),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = imageUrl,
                                onValueChange = { imageUrl = it },
                                label = { Text("URL de la imagen") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6200EE),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            isLoading = true
                                            val updatedEpisode = EpisodeModel(
                                                id = episodeId,
                                                name = name,
                                                date = date,
                                                duration = duration,
                                                imageUrl = imageUrl
                                            )
                                            val success = viewModel.updateEpisode(updatedEpisode)
                                            if (success) {
                                                navController.popBackStack()
                                            }
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text("Guardar Cambios", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

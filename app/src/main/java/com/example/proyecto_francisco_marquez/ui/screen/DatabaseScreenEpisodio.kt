package com.example.proyecto_francisco_marquez.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.model.EpisodeModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreenEpisodio(
    navController: NavHostController,

) {
    val firestoreService = FirestoreService()
    val viewModel: DatabaseViewModel = viewModel(factory = DatabaseViewModelFactory(firestoreService))
    val episodes by viewModel.episodes.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val syncState by viewModel.syncState.observeAsState()

    LaunchedEffect(viewModel.episodes.value) {
        if (viewModel.episodes.value.isNullOrEmpty()) {
            viewModel.getEpisodes()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagen_fondo),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "Episodios",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.getEpisodes() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Recargar",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = { navController.navigate("agregarEpisodioScreen") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.9f))
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                episodes.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(episodes) { episode ->
                            EpisodeCard(navController, episode, onDelete = {
                                viewModel.deleteEpisode(episode.id)
                            })
                        }
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay episodios disponibles", color = Color.Black)
                    }
                }
            }

            syncState?.let { state ->
                when (state) {
                    is DatabaseViewModel.SyncState.Success -> {
                        Toast.makeText(LocalContext.current, state.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    is DatabaseViewModel.SyncState.Error -> {
                        Toast.makeText(
                            LocalContext.current,
                            state.exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }
}
    @Composable
    private fun EpisodeCard(
        navController: NavHostController,
        episode: EpisodeModel,
        onDelete: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = episode.imageUrl.takeIf { it.isNotEmpty() }
                            ?: R.drawable.imagen_fondo,
                        contentDescription = "Imagen del episodio",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.imagen_fondo)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    Text(
                        episode.name,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(
                        label = "Fecha:",
                        value = episode.date
                    )

                    InfoRow(
                        label = "Duración:",
                        value = episode.duration
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { navController.navigate("modificarEpisodioScreen/${episode.id}") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Modificar")
                        }

                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun InfoRow(
        label: String,
        value: String
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(100.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )
        }
    }

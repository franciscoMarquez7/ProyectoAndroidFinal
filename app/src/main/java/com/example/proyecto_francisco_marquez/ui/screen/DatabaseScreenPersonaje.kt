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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.model.CharacterModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreenPersonaje(
    navController: NavHostController,
) {
    val firestoreService = FirestoreService()
    val viewModel: DatabaseViewModel = viewModel(factory = DatabaseViewModelFactory(firestoreService))
    val characters by viewModel.characters.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val syncState by viewModel.syncState.observeAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.characters.value) {
        if (viewModel.characters.value.isNullOrEmpty()) {
            viewModel.getCharacters()
        }
    }

    // Manejar estados de sincronización
    LaunchedEffect(syncState) {
        when (syncState) {
            is DatabaseViewModel.SyncState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Datos cargados correctamente",
                    duration = SnackbarDuration.Short
                )
            }
            is DatabaseViewModel.SyncState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Error al cargar los datos",
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
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
                        "Personajes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.getCharacters() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.Black)
                    }
                    IconButton(onClick = { navController.navigate("agregarPersonajeScreen") }) {
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
                characters.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        items(characters) { character ->
                            CharacterCard(navController, character, onDelete = {
                                viewModel.deleteCharacter(character.id)
                            })
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay personajes disponibles", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterCard(
    navController: NavHostController,
    character: CharacterModel,
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
            // Imagen con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = character.imagenUrl.takeIf { it.isNotEmpty() }
                        ?: R.drawable.imagen_fondo,
                    contentDescription = "Imagen del personaje",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.imagen_fondo)
                )
                
                // Gradiente sobre la imagen
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
                
                // Título sobre la imagen
                Text(
                    character.name,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Información del personaje
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(
                    label = "Estado:",
                    value = character.status
                )
                
                InfoRow(
                    label = "Especie:",
                    value = character.species
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { navController.navigate("modificarPersonajeScreen/${character.id}") },
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

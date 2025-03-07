package com.example.proyecto_francisco_marquez.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.proyecto_francisco_marquez.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.proyecto_francisco_marquez.viewmodel.DatabaseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

data class EpisodeModel(
    val id: String,
    val name: String,
    val episode: String,    // Ejemplo: "S01E01"
    val airDate: String,
    val imagenUrl: String,
    val characters: List<String> = emptyList() // Lista de IDs de personajes en este episodio
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreenEpisodio(
    navController: NavHostController,
    viewModel: DatabaseViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var episodes by remember { mutableStateOf<List<EpisodeModel>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showNoData by remember { mutableStateOf(false) }

    fun reloadEpisodes() {
        isLoading = true
        showNoData = false
        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (auth.currentUser == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sesión expirada. Por favor, vuelve a iniciar sesión", Toast.LENGTH_LONG).show()
                        navController.navigate("login")
                    }
                    return@launch
                }

                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("episodios").get().await()
                
                // Debug: Imprimir todos los documentos y sus datos
                println("DEBUG: Número total de documentos: ${snapshot.size()}")
                snapshot.documents.forEach { doc ->
                    println("DEBUG: ID del documento: ${doc.id}")
                    println("DEBUG: Todos los datos: ${doc.data}")
                    // Imprimir cada campo individualmente
                    println("DEBUG: Campo 'name': ${doc.getString("name")}")
                    println("DEBUG: Campo 'nombre': ${doc.getString("nombre")}")
                    println("DEBUG: Campo 'episode': ${doc.getString("episode")}")
                    println("DEBUG: Campo 'episodio': ${doc.getString("episodio")}")
                }

                episodes = snapshot.documents.mapNotNull { doc ->
                    try {
                        EpisodeModel(
                            id = doc.id,
                            name = doc.getString("name") ?: "Desconocido",
                            episode = doc.getString("duration") ?: "Desconocido",
                            airDate = doc.getString("date") ?: "Desconocido",
                            imagenUrl = doc.getString("imageUrl") ?: "",
                            characters = emptyList()
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Error al procesar documento ${doc.id}: ${e.message}")
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    isLoading = false
                    showNoData = episodes.isEmpty()
                    println("DEBUG: Episodios cargados: $episodes")
                }
            } catch (e: Exception) {
                println("DEBUG: Error general: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoading = false
                    showNoData = true
                    Toast.makeText(
                        context,
                        "Error al cargar episodios: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun deleteEpisode(episodeId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                
                // Primero, obtener todos los personajes vinculados
                val charactersToUpdate = db.collection("personajes")
                    .whereEqualTo("episode_id", episodeId)
                    .get()
                    .await()

                // Actualizar cada personaje para eliminar la referencia al episodio
                charactersToUpdate.documents.forEach { doc ->
                    db.collection("personajes")
                        .document(doc.id)
                        .update("episode_id", "")
                        .await()
                }

                // Finalmente, eliminar el episodio
                db.collection("episodios")
                    .document(episodeId)
                    .delete()
                    .await()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, 
                        "Episodio eliminado y ${charactersToUpdate.size()} personajes actualizados", 
                        Toast.LENGTH_SHORT
                    ).show()
                    reloadEpisodes()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error al eliminar el episodio: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) {
            reloadEpisodes()
        } else {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
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
                    IconButton(onClick = { reloadEpisodes() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Recargar",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = { navController.navigate("agregarEpisodioScreen") }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Agregar",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )

            if (showNoData) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            "NO HAY REGISTROS",
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(episodes) { episode ->
                        EpisodeCard(navController, episode, onDelete = { deleteEpisode(episode.id) })
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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

@Composable
private fun EpisodeCard(
    navController: NavHostController,
    episode: EpisodeModel,
    onDelete: () -> Unit
) {
    var characterCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(episode.id) {
        try {
            coroutineScope.launch(Dispatchers.IO) {
                val count = withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance()
                        .collection("personajes")
                        .whereEqualTo("episode_id", episode.id)
                        .get()
                        .await()
                        .size()
                }
                characterCount = count
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error al cargar personajes: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

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
                    model = episode.imagenUrl.takeIf { it.isNotEmpty() && it != "ee" }
                        ?: R.drawable.imagen_fondo,
                    contentDescription = "Imagen del episodio",
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
                    episode.name,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Información del episodio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoRow(
                    label = "Duración:",
                    value = episode.episode
                )
                
                InfoRow(
                    label = "Fecha:",
                    value = episode.airDate
                )
                
                InfoRow(
                    label = "Personajes:",
                    value = characterCount.toString()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botones
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
                        onClick = {
                            try {
                                if (characterCount > 0) {
                                    navController.navigate("verPersonajesEpisodio/${episode.id}") {
                                        launchSingleTop = true
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No hay personajes registrados para este episodio",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al navegar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ver Personajes")
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
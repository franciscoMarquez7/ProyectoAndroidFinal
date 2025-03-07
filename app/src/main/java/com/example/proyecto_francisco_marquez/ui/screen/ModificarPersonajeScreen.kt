package com.example.proyecto_francisco_marquez.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.ui.TitleStyle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarPersonajeScreen(navController: NavHostController, documentId: String) {
    val db = FirebaseFirestore.getInstance()
    val firestoreService = FirestoreService()
    val scope = rememberCoroutineScope()

    var newName by remember { mutableStateOf("") }
    var newStatus by remember { mutableStateOf("") }
    var newSpecies by remember { mutableStateOf("") }
    var newEpisodeId by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(documentId) {
        db.collection("personajes").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    newName = document.getString("name") ?: ""
                    newStatus = document.getString("status") ?: ""
                    newSpecies = document.getString("species") ?: ""
                    newEpisodeId = document.getString("episode_id") ?: ""
                    newImageUrl = document.getString("imagenUrl") ?: ""
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Modificar Personaje", style = TitleStyle.copy(color = Color.Black)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Imagen del personaje
                        AsyncImage(
                            model = newImageUrl.ifEmpty { "https://via.placeholder.com/150" },
                            contentDescription = "Imagen del personaje",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = newStatus,
                            onValueChange = { newStatus = it },
                            label = { Text("Estado") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = newSpecies,
                            onValueChange = { newSpecies = it },
                            label = { Text("Especie") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = newEpisodeId,
                            onValueChange = { newEpisodeId = it },
                            label = { Text("ID del episodio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = newImageUrl,
                            onValueChange = { newImageUrl = it },
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
                                    val updatedData = mutableMapOf<String, Any>()
                                    if (newName.isNotEmpty()) updatedData["name"] = newName
                                    if (newStatus.isNotEmpty()) updatedData["status"] = newStatus
                                    if (newSpecies.isNotEmpty()) updatedData["species"] = newSpecies
                                    if (newEpisodeId.isNotEmpty()) updatedData["episode_id"] = newEpisodeId
                                    if (newImageUrl.isNotEmpty()) updatedData["imagenUrl"] = newImageUrl

                                    if (updatedData.isNotEmpty()) {
                                        try {
                                            firestoreService.updateCharacter(documentId, updatedData)
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text("Guardar Cambios", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

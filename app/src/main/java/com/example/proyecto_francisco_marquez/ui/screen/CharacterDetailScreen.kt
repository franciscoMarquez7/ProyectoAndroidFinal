package com.example.proyecto_francisco_marquez.ui.screen

import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.proyecto_francisco_marquez.R
import com.example.proyecto_francisco_marquez.ui.ModernButton
import com.example.proyecto_francisco_marquez.ui.TitleStyle
import com.example.proyecto_francisco_marquez.ui.gradientBackground
import kotlinx.coroutines.*

@Composable
fun CharacterDetailScreen(characterId: String, navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo con efecto de oscurecimiento
        Image(
            painter = painterResource(id = R.drawable.imagen_fondo),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (characterId == null) {
                Text("Error: No se encontró el ID del personaje", style = TitleStyle)
                ModernButton(text = "Volver", onClick = { navController.popBackStack() })
                return
            }

            val imageUrl = "https://rickandmortyapi.com/api/character/avatar/$characterId.jpeg"

            AsyncImage(
                model = imageUrl,
                contentDescription = "Character Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModernButton(
                text = "Guardar Imagen",
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val success = saveImageToGallery(imageUrl, context)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                if (success) "¡Imagen guardada!" else "Error al guardar la imagen",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModernButton(
                text = "Volver",
                onClick = { navController.popBackStack() }
            )
        }
    }
}

suspend fun saveImageToGallery(imageUrl: String, context: android.content.Context): Boolean {
    return try {
        val bitmap = withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = (context.imageLoader.execute(request) as? SuccessResult)?.drawable
            (result as? BitmapDrawable)?.bitmap
        }

        if (bitmap == null) return false

        val filename = "Character_${System.currentTimeMillis()}.png"

        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RickAndMortyApp")
        }

        val uri = context.contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return false

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Image saved in gallery!", Toast.LENGTH_SHORT).show()
        }

        true
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
        e.printStackTrace()
        false
    }
}

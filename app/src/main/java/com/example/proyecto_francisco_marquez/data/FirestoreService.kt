package com.example.proyecto_francisco_marquez.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.proyecto_francisco_marquez.model.UiState
import com.example.proyecto_francisco_marquez.model.CharacterModel
import com.example.proyecto_francisco_marquez.model.EpisodeModel

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val charactersCollection = db.collection("personajes")
    private val episodesCollection = db.collection("episodios")

    private fun isUserAuthenticated(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("FirestoreService", "Usuario no autenticado")
            return false
        }
        return true
    }

    private suspend fun verifyAuthentication(): Boolean {
        val currentUser = auth.currentUser ?: return false
        try {
            // Forzar actualización del token
            currentUser.getIdToken(true).await()
            return true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error verificando autenticación", e)
            return false
        }
    }

    // Agregar un personaje
    suspend fun addCharacter(character: Map<String, Any>): Boolean {
        return try {
            charactersCollection.add(character).await() // Usamos .add() para agregar sin un ID especificado
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error agregando personaje: ${e.message}")
            false
        }
    }

    // Actualizar un personaje usando el ID del documento
    suspend fun updateCharacter(characterId: String, updatedData: Map<String, Any>): Boolean {
        return try {
            charactersCollection.document(characterId).update(updatedData).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error actualizando personaje: ${e.message}")
            false
        }
    }

    // Eliminar un personaje
    suspend fun deleteCharacter(characterId: String): Boolean {
        return try {
            // Eliminar un personaje usando su ID
            charactersCollection.document(characterId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error eliminando personaje: ${e.message}")
            false
        }
    }

    suspend fun getCharacters(): UiState<List<CharacterModel>> {
        if (!isUserAuthenticated()) return UiState.Error("Usuario no autenticado")
        
        return try {
            val snapshot = charactersCollection.get().await()
            val characters = snapshot.documents.map { doc ->
                CharacterModel(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    status = doc.getString("status") ?: "",
                    species = doc.getString("species") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    episode_id = doc.getString("episode_id") ?: ""
                )
            }
            UiState.Success(characters)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error obteniendo personajes: ${e.message}")
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun addEpisode(episode: Map<String, Any>): Boolean {
        return try {
            episodesCollection.add(episode).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEpisode(episodeId: String, episode: Map<String, Any>): Boolean {
        return try {
            episodesCollection.document(episodeId).update(episode).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getEpisodeById(episodeId: String): UiState<EpisodeModel> {
        return try {
            val doc = episodesCollection.document(episodeId).get().await()
            if (doc.exists()) {
                val episode = doc.toObject(EpisodeModel::class.java)
                if (episode != null) {
                    UiState.Success(episode)
                } else {
                    UiState.Error("Error al convertir el documento")
                }
            } else {
                UiState.Error("Episodio no encontrado")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getEpisodes(): UiState<List<EpisodeModel>> {
        return try {
            val snapshot = episodesCollection.get().await()
            val episodes = snapshot.documents.mapNotNull { 
                it.toObject(EpisodeModel::class.java) 
            }
            UiState.Success(episodes)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getCharacterById(characterId: String): UiState<CharacterModel> {
        return try {
            val doc = charactersCollection.document(characterId).get().await()
            if (doc.exists()) {
                val character = CharacterModel(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    status = doc.getString("status") ?: "",
                    species = doc.getString("species") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    episode_id = doc.getString("episode_id") ?: ""
                )
                UiState.Success(character)
            } else {
                UiState.Error("Personaje no encontrado")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getCharactersByEpisodeId(episodeId: String): UiState<List<CharacterModel>> {
        return try {
            val snapshot = charactersCollection
                .whereEqualTo("episode_id", episodeId)
                .get()
                .await()
            
            val characters = snapshot.documents.mapNotNull { doc ->
                CharacterModel(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    status = doc.getString("status") ?: "",
                    species = doc.getString("species") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    episode_id = doc.getString("episode_id") ?: ""
                )
            }
            UiState.Success(characters)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun addCharacterToEpisode(characterId: String, episodeId: String): UiState<Boolean> {
        return try {
            val characterUpdate = mapOf("episode_id" to episodeId)
            charactersCollection.document(characterId).update(characterUpdate).await()
            UiState.Success(true)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun removeCharacterFromEpisode(characterId: String, episodeId: String): UiState<Boolean> {
        return try {
            val characterUpdate = mapOf("episode_id" to "")
            charactersCollection.document(characterId).update(characterUpdate).await()
            UiState.Success(true)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun deleteEpisode(episodeId: String): UiState<Boolean> {
        return try {
            // Primero, actualizar todos los personajes que pertenecen a este episodio
            val charactersToUpdate = charactersCollection
                .whereEqualTo("episode_id", episodeId)
                .get()
                .await()

            // Actualizar cada personaje para eliminar la referencia al episodio
            charactersToUpdate.documents.forEach { doc ->
                charactersCollection
                    .document(doc.id)
                    .update("episode_id", "")
                    .await()
            }

            // Luego, eliminar el episodio
            episodesCollection.document(episodeId).delete().await()
            UiState.Success(true)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }
}

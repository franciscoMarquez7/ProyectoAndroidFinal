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

    // Agregar un personaje
    suspend fun addCharacter(characterId: Map<String, Any>): Boolean {
        return try {
            charactersCollection.add(characterId)
                .await() // Usamos .add() para agregar sin un ID especificado
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error agregando personaje: ${e.message}")
            false
        }
    }

    suspend fun getCharacterById(characterId: String): CharacterModel? {
        return try {
            val document = charactersCollection.document(characterId).get().await()
            document.toObject(CharacterModel::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error obteniendo el personaje: ${e.message}")
            null
        }
    }
    suspend fun getEpisodeById(episodeId: String): EpisodeModel? {
        return try {
            val document = episodesCollection.document(episodeId).get().await()
            val data = document.data
            if (data != null) {
                EpisodeModel(
                    id = document.id,
                    name = data["name"] as? String ?: "",
                    date = data["date"] as? String ?: "",
                    duration = data["duration"] as? String ?: "",
                    imageUrl = data["imageUrl"] as? String ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error obteniendo el episodio: ${e.message}")
            null
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
    suspend fun updateEpisode(episodeId: String, updatedData: Map<String, Any>): Boolean {
        return try {
            episodesCollection.document(episodeId).update(updatedData).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error actualizando episodio: ${e.message}")
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
            val characters = snapshot.documents.mapNotNull { doc ->
                try {
                    CharacterModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        status = doc.getString("status") ?: "",
                        species = doc.getString("species") ?: "",
                        imagenUrl = doc.getString("imagenUrl") ?: "",
                        episode_id = doc.getString("episode_id") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirestoreService", "Error procesando documento Firestore: ${doc.id}")
                    null
                }
            }
            UiState.Success(characters)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error obteniendo personajes: ${e.message}")
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun addEpisode(episodeId: Map<String, Any>): Boolean {
        return try {
            episodesCollection.add(episodeId).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun getEpisodes(): UiState<List<EpisodeModel>> {
        return try {
            val snapshot = episodesCollection.get().await()
            val episodes = snapshot.documents.mapNotNull { doc ->
                try {
                    EpisodeModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        date = doc.getString("date") ?: "",
                        duration = doc.getString("duration") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirestoreService", "Error procesando documento Firestore: ${doc.id}")
                    null
                }
            }
            UiState.Success(episodes)
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
                    imagenUrl = doc.getString("imagenUrl") ?: "",
                    episode_id = doc.getString("episode_id") ?: ""
                )
            }
            UiState.Success(characters)
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

package com.example.proyecto_francisco_marquez.data

import com.example.proyecto_francisco_marquez.model.CharacterModel
import com.example.proyecto_francisco_marquez.model.EpisodeModel
import com.example.proyecto_francisco_marquez.model.UiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EpisodesRepository(
    private val firestoreService: FirestoreService
) {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getEpisodes(): UiState<List<EpisodeModel>> = withContext(Dispatchers.IO) {
        try {
            firestoreService.getEpisodes()
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getEpisodeById(episodeId: String): UiState<EpisodeModel> = withContext(Dispatchers.IO) {
        try {
            firestoreService.getEpisodeById(episodeId)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun addEpisode(episode: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            firestoreService.addEpisode(episode)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateEpisode(episodeId: String, episode: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            firestoreService.updateEpisode(episodeId, episode)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteEpisode(episodeId: String): UiState<Boolean> = withContext(Dispatchers.IO) {
        try {
            firestoreService.deleteEpisode(episodeId)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getEpisodeWithCharacters(episodeId: String): Pair<EpisodeModel?, List<CharacterModel>> {
        val episodeDoc = db.collection("episodios").document(episodeId).get().await()
        val episode = episodeDoc.toObject(EpisodeModel::class.java)?.copy(id = episodeDoc.id)

        val charactersSnapshot = db.collection("personajes")
            .whereEqualTo("episode_id", episodeId)  // 🔹 Ahora coincide con Firestore
            .get()
            .await()

        val characters = charactersSnapshot.documents.map { doc ->
            CharacterModel(
                id = doc.id,
                name = doc.getString("name") ?: "Desconocido",
                imageUrl = doc.getString("imagenUrl") ?: "",
                status = doc.getString("status") ?: "Desconocido",
                species = doc.getString("species") ?: "Desconocido",
                episode_id = doc.getString("episode_id") ?: ""  // 🔹 Asegurar conversión correcta
            )
        }

        return Pair(episode, characters)
    }
} 
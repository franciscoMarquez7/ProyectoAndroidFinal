package com.example.proyecto_francisco_marquez.data

import com.example.proyecto_francisco_marquez.model.EpisodeModel
import com.example.proyecto_francisco_marquez.model.UiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface EpisodeService {
    suspend fun getEpisodes(): UiState<List<EpisodeModel>>
    suspend fun getEpisodeById(episodeId: String): UiState<EpisodeModel>
    suspend fun addEpisode(episode: Map<String, Any>): Boolean
    suspend fun updateEpisode(episodeId: String, episode: Map<String, Any>): Boolean
    suspend fun deleteEpisode(episodeId: String): UiState<Boolean>
}

class EpisodeServiceImpl : EpisodeService {
    private val firestoreService = FirestoreService()

    override suspend fun getEpisodes(): UiState<List<EpisodeModel>> {
        return firestoreService.getEpisodes()
    }

    override suspend fun getEpisodeById(episodeId: String): UiState<EpisodeModel> {
        return firestoreService.getEpisodeById(episodeId)
    }

    override suspend fun addEpisode(episode: Map<String, Any>): Boolean {
        return firestoreService.addEpisode(episode)
    }

    override suspend fun updateEpisode(episodeId: String, episode: Map<String, Any>): Boolean {
        return firestoreService.updateEpisode(episodeId, episode)
    }

    override suspend fun deleteEpisode(episodeId: String): UiState<Boolean> {
        return firestoreService.deleteEpisode(episodeId)
    }
}

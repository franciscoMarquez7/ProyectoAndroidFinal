package com.example.proyecto_francisco_marquez.model

data class EpisodeModel(
    val id: String = "",
    val name: String = "",
    val airDate: String = "",
    val episode: String = "",
    val characters: List<String> = emptyList()
) {
    fun isValid(): Boolean {
        return name.isNotBlank() && airDate.isNotBlank() && episode.isNotBlank()
    }
} 
package com.example.proyecto_francisco_marquez.model

data class CharacterModel(
    val id: String = "",
    val episode_id: String = "",
    val name: String = "",
    val status: String = "",
    val species: String = "",
    val imagenUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "episode_id" to episode_id,
            "name" to name,
            "status" to status,
            "species" to species,
            "imagenUrl" to imagenUrl
        )
    }
}

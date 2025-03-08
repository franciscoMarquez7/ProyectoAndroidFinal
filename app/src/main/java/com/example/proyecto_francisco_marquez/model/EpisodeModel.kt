package com.example.proyecto_francisco_marquez.model

data class EpisodeModel(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val duration: String = "",
    val imageUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "date" to date,
            "duration" to duration,
            "imageUrl" to imageUrl
        )
    }
}

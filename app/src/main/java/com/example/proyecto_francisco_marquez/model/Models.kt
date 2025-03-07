package com.example.proyecto_francisco_marquez.model

data class CharacterModel(
    val id: String = "",
    val episode_id: String = "",
    val name: String = "",
    val status: String = "",
    val species: String = "",
    val imageUrl: String = ""
) {
    val image: String
        get() = imageUrl
}

// Mantener cualquier otro modelo que esté aquí y que no esté en otros archivos 
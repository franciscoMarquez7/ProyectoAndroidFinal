package com.example.proyecto_francisco_marquez.data.api

import com.example.proyecto_francisco_marquez.data.model.RickAndMortyResponse
import retrofit2.http.GET

// Interfaz del servicio de la API
interface ApiService {
    @GET("character")
    suspend fun getCharacters(): RickAndMortyResponse
}
package com.example.proyecto_francisco_marquez.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_francisco_marquez.data.api.ApiService
import com.example.proyecto_francisco_marquez.data.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CharacterViewModel : ViewModel() {
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val api = Retrofit.Builder()
        .baseUrl("https://rickandmortyapi.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun fetchCharacters() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = api.getCharacters()
                _characters.value = response.results
            } catch (e: Exception) {
                _error.value = "Error al cargar los personajes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCharacter(id: Any) {

    }
} 
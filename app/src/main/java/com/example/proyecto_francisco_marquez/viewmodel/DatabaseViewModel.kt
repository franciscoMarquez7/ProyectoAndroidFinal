package com.example.proyecto_francisco_marquez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_francisco_marquez.data.FirestoreService
import com.example.proyecto_francisco_marquez.model.CharacterModel
import com.example.proyecto_francisco_marquez.model.EpisodeModel
import com.example.proyecto_francisco_marquez.model.UiState
import kotlinx.coroutines.launch

class DatabaseViewModel(private val firestoreService: FirestoreService) : ViewModel() {

    private val _characters = MutableLiveData<List<CharacterModel>>()
    val characters: LiveData<List<CharacterModel>> get() = _characters

    private val _episodes = MutableLiveData<List<EpisodeModel>>()
    val episodes: LiveData<List<EpisodeModel>> get() = _episodes

    private val _syncState = MutableLiveData<SyncState>()
    val syncState: LiveData<SyncState> get() = _syncState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    sealed class SyncState {
        data class Success(val message: String) : SyncState()
        data class Error(val exception: Throwable) : SyncState()
    }

    suspend fun addCharacter(character: CharacterModel): Boolean {
        return try {
            _isLoading.value = true
            firestoreService.addCharacter(character.toMap()) // Usa el FirestoreService
            _syncState.value = SyncState.Success("Personaje agregado")
            getCharacters() // Recargar lista
            _isLoading.value = false
            true // Si todo funciona, devuelve true
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            _isLoading.value = false
            false // Si hay un error, devuelve false
        }
    }


    fun getCharacters() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val characterList = firestoreService.getCharacters()
                if (characterList is UiState.Success) {
                    _characters.value = characterList.data
                    if (_syncState.value !is SyncState.Success) {
                        _syncState.value = SyncState.Success("Personajes cargados")
                    }
                } else {
                    _syncState.value = SyncState.Error(Exception("Error al obtener personajes"))
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e)
            }
            _isLoading.value = false
        }
    }

    fun deleteCharacter(characterId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                firestoreService.deleteCharacter(characterId)
                _syncState.value = SyncState.Success("Personaje eliminado")
                getCharacters()
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e)
            }
            _isLoading.value = false
        }
    }

    suspend fun getCharacterById(characterId: String): CharacterModel? {
        return try {
            firestoreService.getCharacterById(characterId)
        } catch (e: Exception) {
            null
        }
    }

    // Actualizar un personaje existente
    suspend fun updateCharacter(character: CharacterModel): Boolean {
        return try {
            firestoreService.updateCharacter(character.id, character.toMap())
            _syncState.value = SyncState.Success("Personaje actualizado")
            getCharacters() // Recargar la lista después de la actualización
            true
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            false
        }
    }
    suspend fun updateEpisode(episode: EpisodeModel): Boolean {
        return try {
            firestoreService.updateEpisode(episode.id, episode.toMap())
            _syncState.value = SyncState.Success("Episodio actualizado")
            getEpisodes() // Recargar la lista después de la actualización
            true
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            false
        }
    }

    suspend fun addEpisode(episode: EpisodeModel): Boolean {
        return try {
            _isLoading.value = true
            firestoreService.addEpisode(episode.toMap()) // Usa FirestoreService
            _syncState.value = SyncState.Success("Episodio agregado")
            getEpisodes() // Recargar lista
            _isLoading.value = false
            true // Si todo funciona, devuelve true
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            _isLoading.value = false
            false // Si hay un error, devuelve false
        }
    }


    fun getEpisodes() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val episodeList = firestoreService.getEpisodes()
                if (episodeList is UiState.Success) {
                    _episodes.value = episodeList.data
                    _syncState.value = SyncState.Success("Episodios cargados")
                } else {
                    _syncState.value = SyncState.Error(Exception("Error al obtener episodios"))
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e)
            }
            _isLoading.value = false
        }
    }

    fun deleteEpisode(episodeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                firestoreService.deleteEpisode(episodeId)
                _syncState.value = SyncState.Success("Episodio eliminado")
                getEpisodes()
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e)
            }
            _isLoading.value = false
        }
    }

    fun getCharactersByEpisodeId(episodeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val characterList = firestoreService.getCharactersByEpisodeId(episodeId)
                if (characterList is UiState.Success) {
                    _characters.value = characterList.data
                    _syncState.value = SyncState.Success("Personajes del episodio cargados")
                } else {
                    _syncState.value = SyncState.Error(Exception("Error al obtener personajes del episodio"))
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error(e)
            }
            _isLoading.value = false
        }
    }


    suspend fun getEpisodeById(episodeId: String): EpisodeModel? {
        return try {
            firestoreService.getEpisodeById(episodeId)
        } catch (e: Exception) {
            null
        }
    }
}
